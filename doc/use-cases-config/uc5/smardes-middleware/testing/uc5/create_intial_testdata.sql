delete from uc5_live_data;

insert into uc5_live_data (
	modtime, equipment, temperature_setp, temperature_act,
	temperature_LL, temperature_L, temperature_HH, temperature_H,
	pressure, numrev
) values (
	SYSUTCDATETIME(), 'EQ1', 80, 75, 70, 72, 90, 88, 100, 450
);

insert into uc5_live_data (
	modtime, equipment, temperature_setp, temperature_act,
	temperature_LL, temperature_L, temperature_HH, temperature_H,
	pressure, numrev
) values (
	SYSUTCDATETIME(), 'EQ2', 80, 77, 70, 72, 90, 88, 105, 480
);
