namespace capex;

using { capex as db } from '../schema';

entity MasterCERType : db.fingerprints {
    key ID : Integer;
    Type    : String(30);
}

entity MasterStatus : db.fingerprints {
    key ID : Integer;
    Status    : String(30);
}

entity MasterStage : db.fingerprints {
    key ID : Integer;
    Stage    : String(30);
}

entity MasterAmount : db.fingerprints {
    key ID : Integer;
    Range   : String;
    RangeMin   : Double;
    RangeMax   : Double;
}

entity MasterAssetType : db.fingerprints {
    key ID : Integer;
    Type    : String(30);
}

entity MasterExpenseType : db.fingerprints {
    key ID : Integer;
    Type    : String(30);
}

entity MasterRecurringExpense : db.fingerprints {
    key ID : Integer;
    Expense    : String(30);
}

entity MasterProjectedReturn : db.fingerprints {
    key ID : Integer;
    Value    : String(30);
}

entity MasterTAT : db.fingerprints {
    key ID: Integer;
    CERType: Association to capex.MasterCERType on CERType.ID = $self.CERType_ID;
    TATLevels: Composition of many MasterTATLevel on TATLevels.TAT_ID = $self.ID;
    CERType_ID: Integer;
    Description: String;
}

entity MasterTATLevel : db.fingerprints {
    key ID: Integer;
    TATUser: Association to capex.Employee on upper(TATUser.Email) = upper($self.TATUserEmail);
    TAT: Association to capex.MasterTAT on TAT.ID = $self.TAT_ID;
    TAT_ID: Integer;
    TATUserEmail: String(300);
    Level: Integer;
    TATDurationMinutes: Integer;
}

entity MasterCostCenter : db.fingerprints{
    key ID :  Integer;
    CostCenter :  String(30);
}

entity MasterProfitCenter : db.fingerprints{
    key ID :  Integer;
    ProfitCenter :  String(30);
}