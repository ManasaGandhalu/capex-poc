namespace capex;

using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';
using { capex as db} from '../schema';

entity CER : cuid, db.fingerprints {
    CERType         : Association to db.MasterCERType;
    Status          : Association to db.MasterStatus ; 
    CurrentStage    : Association to db.MasterStage;  
    ParentCER       : Association to CER;  
    CurrentApprovalLevel :  Association to db.MasterTATLevel;
    CERLineItems     : Composition of many CERLineItem on $self.ID = CERLineItems.CER_ID;
    CERApprovals     :  Composition of many CERApproval on $self.ID = CERApprovals.CER_ID;
    CERCode   : String;
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
    BudgetaryTotalCost  : Double default 0.0;
    AgainstBudgetaryTotalCost : Double default 0.0   
}

entity CERLineItem : cuid, db.fingerprints {
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

entity CERApproval : cuid, db.fingerprints {
    ApprovalLevel : String(30);
    ApprovalStatus : String(50);
    CER_ID         : UUID ;
}
entity MediaStore : cuid, db.fingerprints {
    mediaId: String;
    mediaName: String;
    content: LargeBinary;
    contentType: String;
    url: String;
    directoryName: String;
    directoryId: String;
    status: String;
    mediaSize: Int64;
}

entity Employee :  cuid, db.fingerprints{
    FirstName   : String(30);
    LastName    : String(30);
    Email       : String(30);
    Mobile      : Integer
}

entity ApprovalQuery: cuid, db.fingerprints {
    approvalId: Int64;
    comment: String;
    mediaStoreId: UUID;
}

// @cds.persistence.skip
// entity ApprovalReply: cuid, db.fingerprints {
//     approvalQueryId: UUID;
//     approvalId: Int64;
//     comment: String;
//     attachment: LargeBinary @Core.MediaType: attachmentType @Core.ContentDisposition.Filename: attachmentName @Core.ContentDisposition.Type: 'attachment';
//     attachmentName : String;
//     attachmentType: String @Core.IsMediaType;
// }
