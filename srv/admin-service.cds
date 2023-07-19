using { capex as db } from '../db/schema';

service AdminService {
    entity CER as projection on db.CER;
    entity CERApproval as projection on db.CERApproval;
    entity MasterCERType as projection on db.MasterCERType;
    entity MasterStage as projection on db.MasterStage;
    entity MasterStatus as projection on db.MasterStatus;
    entity Employee as projection on db.Employee;
    entity MediaStore as projection on db.MediaStore;
    entity MasterAmount as projection on db.MasterAmount;
    entity MasterAssetType as projection on db.MasterAssetType;
    entity MasterExpenseType as projection on db.MasterExpenseType;
    entity MasterProjectedReturn as projection on db.MasterProjectedReturn;
    entity MasterTATLevel as projection on db.MasterTATLevel;
    entity MasterRecurringExpense as projection on db.MasterRecurringExpense;

}
