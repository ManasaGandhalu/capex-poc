namespace capex;
using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';

aspect fingerprints: managed {
    IsArchived: Boolean default false;
}

entity CER : cuid, fingerprints{
    CERType         : Association to MasterCERType;
    Status          : Association to MasterStatus ; 
    CurrentStage    : Association to MasterStage;  
    CurrentApprovalLevel :  Association to MasterTATLevel;
    CERLineItems     : Composition of many CERLineItem on $self.ID = CERLineItems.CER_ID;
    CERApprovals     :  Composition of many CERApproval on $self.ID = CERApprovals.CER_ID;
    CERNumber   : Integer;
    BudgetaryID     : Integer; 
    WorkflowRequestId : String(50);  
    TotalApprovalLevels :  Integer;
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
    ProjectCompletionTime : String(100);
    CostCenter      :String(50);
    CerLocation     : String(50);

    
}

entity CERLineItem : cuid, fingerprints {
    Particular : String(50);
    Description : String(200);
    Unit        : Integer;
    UOM         : String(30);
    PerUnitCost : Double;
    NetCost     : Double;
    Tax         : Double;
    GrossCost   : Double;
    CER_ID       : UUID;
}
entity CERApproval : cuid,fingerprints {
    ApprovalLevel : String(30);
    ApprovalStatus : String(50);
    CER_ID         : UUID ;
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

entity MasterAmount : fingerprints {
    key ID : Integer;
    Range   : String;
    RangeMin   : Double;
    RangeMax   : Double;
}

entity MasterTATLevel : fingerprints {
    key ID : Integer;
    Level   : String(30);
}

entity MasterAssetType : fingerprints {
    key ID : Integer;
    Type    : String(30);
}

entity MasterExpenseType : fingerprints {
    key ID : Integer;
    Type    : String(30);
}

entity MasterRecurringExpense : fingerprints {
    key ID : Integer;
    Expense    : String(30);
}

entity MasterProjectedReturn : fingerprints {
    key ID : Integer;
    Value    : String(30);
}

