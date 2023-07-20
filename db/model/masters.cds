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

entity MasterTATLevel : db.fingerprints {
    key ID : Integer;
    Level   : String(30);
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

entity MasterTATUser : db.fingerprints {
    key ID      : Integer;
    FirstName   : String(30);
    LastName    : String(30);
    Email       : String(30);
}