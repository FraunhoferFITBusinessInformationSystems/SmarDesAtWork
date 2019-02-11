
    create table SMARDES_LASTEND (
       UUID_ BINARY(16) not null,
        LASTDATE1_ timestamp,
        LASTDATE2_ timestamp,
        LASTDATE3_ timestamp,
        LASTID1_ bigint,
        LASTID2_ bigint,
        LASTID3_ bigint,
        LASTSTRING1_ varchar(1000),
        LASTSTRING2_ varchar(1000),
        LASTSTRING3_ varchar(1000),
        NAME_ varchar(1000) not null,
        primary key (UUID_)
    );

    create table SMARDES_RESOURCE (
       UUID_ BINARY(16) not null,
        LASTACCESSED_ timestamp not null,
        MIMETYPE_ varchar(100) not null,
        NAME_ varchar(1000) not null,
        primary key (UUID_)
    );

    create table SMARDES_TODOLISTINSTANCE (
       UUID_ BINARY(16) not null,
        ABORTEDAT_ timestamp,
        ABORTEDBY_ varchar(1000),
        CLOSEDAT_ timestamp,
        CLOSEDBY_ varchar(1000),
        DEFINITIONID_ varchar(1000) not null,
        DOMAIN_ varchar(1000) not null,
        STARTEDAT_ timestamp not null,
        STARTEDBY_ varchar(1000) not null,
        primary key (UUID_)
    );

    create table SMARDES_TODOLISTINSTANCE_CONTEXT (
       instance_id BINARY(16) not null,
        value varchar(255),
        name varchar(255) not null,
        primary key (instance_id, name)
    );

    create table SMARDES_TODOLISTSTEP (
       UUID_ BINARY(16) not null,
        CLOSEDAT_ timestamp not null,
        CLOSEDBY_ varchar(1000) not null,
        STEP_ integer not null,
        INSTANCE_ BINARY(16) not null,
        primary key (UUID_)
    );

    alter table SMARDES_LASTEND 
       add constraint UK_mk9fyo4llj1gk0u3882blkl3m unique (NAME_);

    alter table SMARDES_TODOLISTINSTANCE_CONTEXT 
       add constraint FK_TODOLIST_CONTEXT_INSTANCE 
       foreign key (instance_id) 
       references SMARDES_TODOLISTINSTANCE;

    alter table SMARDES_TODOLISTSTEP 
       add constraint FK_TODOLIST_STEP_INSTANCE 
       foreign key (INSTANCE_) 
       references SMARDES_TODOLISTINSTANCE;
