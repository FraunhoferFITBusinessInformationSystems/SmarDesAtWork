
    alter table SMARDES_TODOLISTINSTANCE_CONTEXT 
       drop constraint FK_TODOLIST_CONTEXT_INSTANCE;

    alter table SMARDES_TODOLISTSTEP 
       drop constraint FK_TODOLIST_STEP_INSTANCE;

    drop table SMARDES_LASTEND if exists;

    drop table SMARDES_RESOURCE if exists;

    drop table SMARDES_TODOLISTINSTANCE if exists;

    drop table SMARDES_TODOLISTINSTANCE_CONTEXT if exists;

    drop table SMARDES_TODOLISTSTEP if exists;
