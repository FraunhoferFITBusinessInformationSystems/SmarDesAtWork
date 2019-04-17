DROP TABLE IF EXISTS [uc5_live_data];

CREATE TABLE [uc5_live_data](
                [id] [int] IDENTITY(1,1) NOT NULL,
                [modtime] [datetime] NULL,
                [equipment] [nvarchar](8) NULL,
                [temperature_setp] [decimal](6, 3) NULL,
                [temperature_act] [decimal](6, 3) NULL,
                [temperature_LL] [decimal](6, 3) NULL,
                [temperature_L] [decimal](6, 3) NULL,
                [temperature_HH] [decimal](6, 3) NULL,
                [temperature_H] [decimal](6, 3) NULL,
                [pressure] [decimal](7, 3) NULL,
                [numrev] [decimal](7, 3) NULL,
CONSTRAINT [PK_uc5_live_data] PRIMARY KEY CLUSTERED                
(
               [id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY];
