using { capex as db } from '../db/schema';

service AdminService {
    entity CER as projection on db.CER;
    entity MasterCERType as projection on db.MasterCERType;
    entity MasterStage as projection on db.MasterStage;
    entity MasterStatus as projection on db.MasterStatus;

}
