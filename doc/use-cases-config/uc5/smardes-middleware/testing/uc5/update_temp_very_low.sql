update uc5_live_data
set temperature_act = 69, modtime = SYSUTCDATETIME()
where equipment = 'EQ1';
