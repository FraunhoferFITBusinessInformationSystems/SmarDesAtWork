update uc5_live_data
set pressure = pressure + 10, numrev = numrev + 1, modtime = SYSUTCDATETIME()
where equipment = 'EQ1';
