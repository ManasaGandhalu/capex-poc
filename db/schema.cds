namespace capex;
using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';

aspect fingerprints: managed {
    IsArchived: Boolean default false;
}

entity CER : fingerprints{
    key CERNumber   : Integer;
    BudgetaryID     : Integer;
    CurrentStage    : String(20) ;  
    Status          : String(20) ; 
    TATLevel        : String(10);
    TATUser         : String(30);
    Amount          : Double;
    Site            : String(50);
    ProfitCenter    : String(20);
    ProjetLeader    : String(30);
    ProjectTitle    : String(50);
    Projectdescription : String(100);
    ProjectJustification : String(500);
    CapexDriver     : String(30);
    Currency        : String(10);
    ProjectValue    : Double;
    ProjectCompletionTime : String(100)
}

entity MediaStore : fingerprints {
    key ID      : Integer;
    FileName    : String(30);
    ContentType : String(30);
    Size        : Double;
}

entity Employee :  cuid,fingerprints{
    FirstName   : String(30);
    LastName    : String(30);
    Email       : String(30);
    Mobile      : Integer
}

entity MasterCERType : fingerprints {
    key ID : Integer;
    Type    : String(30);
}

entity MasterStatus : fingerprints {
    key ID : Integer;
    Status    : String(30);
}

entity MasterStage : fingerprints {
    key ID : Integer;
    Stage    : String(30);
}