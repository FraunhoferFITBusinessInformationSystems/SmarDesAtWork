/*******************************************************************************
 * Copyright (C) 2018-2019 camLine GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.camline.projects.smardes.rule;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversationManager {
	private static final Logger logger = LoggerFactory.getLogger(ConversationManager.class);

	public enum State {
		INIT,
		PENDING,
		ACCEPTED,
		REJECTED,
		EXPIRED,
		CLOSED;

		public boolean isPending() {
			return this == State.PENDING;
		}

		public boolean isAccepted() {
			return this == State.ACCEPTED;
		}

		public boolean isFinished() {
			return this == State.REJECTED || this == State.CLOSED || this == State.EXPIRED;
		}
	}

	private final String conversationName;
	private final boolean parallelMode;
	private final Map<String, ScheduledFuture<?>> timeoutHandlers;
	private final List<Pair<State, OffsetDateTime>> protocol;

	private State state;
	private ListIterator<String> usersIterator;
	private String currentAssignee;
	private Map<String, OffsetDateTime> users;


	ConversationManager(final String conversationName, final boolean parallelMode) {
		this.conversationName = conversationName;
		this.parallelMode = parallelMode;
		this.timeoutHandlers = new ConcurrentHashMap<>();
		this.state = State.INIT;
		this.protocol = new ArrayList<>();
		protocol.add(Pair.of(State.INIT, OffsetDateTime.now()));
	}

	public boolean isParallelMode() {
		return parallelMode;
	}

	public void setUsers(final List<String> userList) {
		this.users = new LinkedHashMap<>();
		for (String user : userList) {
			users.put(user, null);
		}

		this.usersIterator = parallelMode ? null : userList.listIterator();
		this.state = State.PENDING;
	}

	List<String> getUsers() {
		return new ArrayList<>(users.keySet());
	}

	public synchronized State getState() {
		return state;
	}

	public synchronized String getCurrentAssignee() {
		return currentAssignee;
	}

	public List<Pair<State, OffsetDateTime>> getProtocol() {
		return protocol;
	}

	/**
	 * Find the next assignee in the user list.
	 *
	 * The method returns a triple of values that depend on each other. There are
	 * these four cases:
	 * <ul>
	 * <li>State is not PENDING anymore: (current state, null, FALSE)</li>
	 * <li>Assignee has already changed in between (e.g. by timeout): (current
	 * state, null, FALSE)</li>
	 * <li>No next assignee anymore: (REJECTED, null, TRUE)</li>
	 * <li>Found a next assignee: (PENDING, next assignee, FALSE)
	 * </ul>
	 * The last value (Boolean) indicates if the assignee and/or state changed at
	 * all. In all other cases the assignee already changed or the conversation
	 * state is not PENDING. The method might be called from different threads and
	 * is therefore synchronized.
	 *
	 * @param lastAssignee
	 *            pass the last assignee to check if it already changed
	 * @return a triple of conversation state, new assignee and if the state changed
	 *         or not
	 */
	public synchronized Triple<State, String, Boolean> nextAssignee(final String lastAssignee) {
		if (!checkPending()) {
			return Triple.of(state, null, Boolean.FALSE);
		}

		if (parallelMode) {
			/*
			 * In parallel mode we get only called at most on conversation timeout. Therefore
			 * this leads immediately to state REJECTED.
			 */
			logger.warn("There is no 'next assignee' in parallel mode. Finish conversation as rejected.");
			this.currentAssignee = null;
			finish(State.REJECTED);
			return Triple.of(state, currentAssignee, Boolean.TRUE);
		}

		if (lastAssignee != null && !lastAssignee.equals(currentAssignee)) {
			logger.info("Assignee already changed in between: {} != {}", lastAssignee, currentAssignee);
			return Triple.of(state, null, Boolean.FALSE);
		}

		users.put(lastAssignee, OffsetDateTime.now());

		if (!usersIterator.hasNext()) {
			logger.warn("No assignees anymore. Finish Conversation as rejected.");
			this.currentAssignee = null;
			finish(State.REJECTED);
			return Triple.of(state, currentAssignee, Boolean.TRUE);
		}

		cancelCurrentTimeoutHandler();

		final int nextIndex = usersIterator.nextIndex();
		this.currentAssignee = usersIterator.next();
		logger.info("Chosen next assignee {}/{}: {}", Integer.valueOf(nextIndex + 1), Integer.valueOf(users.size()),
				currentAssignee);
		return Triple.of(state, currentAssignee, Boolean.FALSE);
	}

	public void registerTimeoutHandler(final String assignee, final ScheduledFuture<?> future) {
		timeoutHandlers.put(assignee, future);
	}

	private void cancelCurrentTimeoutHandler() {
		if (currentAssignee == null) {
			return;
		}

		final ScheduledFuture<?> future = timeoutHandlers.get(currentAssignee);
		if (future != null) {
			future.cancel(false);
			timeoutHandlers.remove(currentAssignee);
		}
	}

	/*
	 * This method must be only called synchronized.
	 */
	private void finish(final State finishState) {
		this.state = finishState;
		protocol.add(Pair.of(state, OffsetDateTime.now()));

		/*
		 * Kill all timeout handlers gracefully. It does not matter if there
		 * is one still executing.
		 */
		timeoutHandlers.values().forEach(future -> future.cancel(false));
		timeoutHandlers.clear();

		/*
		 * Move iterator to the end
		 */
		while (usersIterator != null && usersIterator.hasNext()) {
			usersIterator.next();
		}

		logger.info("Set conversation {} as {}", conversationName, state);
	}

	public synchronized boolean accept(String userId) {
		if (!checkPending()) {
			return false;
		}

		if (userId != null && !users.containsKey(userId)) {
			logger.warn("User {} is not known in current conversation user group {}.", userId, users);
			return false;
		}

		if (parallelMode) {
			this.currentAssignee = userId;
		}

		finish(State.ACCEPTED);

		return true;
	}

	public synchronized boolean reject(String userId) {
		if (!checkPending()) {
			return false;
		}

		if (users.containsKey(userId) && users.get(userId) == null) {
			users.put(userId, OffsetDateTime.now());
		}
		boolean allRejected = users.entrySet().stream().allMatch(entry -> entry.getValue() != null);
		if (allRejected) {
			finish(State.REJECTED);
			return true;
		}
		return false;
	}

	private boolean checkPending() {
		if (state != State.PENDING) {
			logger.warn("Conversation already finished as {}.", state);
			return false;
		}
		return true;
	}

	public synchronized boolean tryClose() {
		if (state.isFinished()) {
			return false;
		}

		finish(State.CLOSED);

		return true;
	}

	public synchronized boolean tryExpire() {
		if (state.isFinished()) {
			return false;
		}

		finish(State.EXPIRED);

		return true;
	}
}
