namespace capex;

using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';
using { capex as db} from '../schema';

entity CER : cuid, db.fingerprints {
    CERType         : Association to db.MasterCERType;
    Status          : Association to db.MasterStatus ; 
    CurrentStage    : Association to db.MasterStage;  
    ParentCER       : Association to CER;  
    CERLineItems     : Composition of many CERLineItem on $self.ID = CERLineItems.CER_ID;
    CERApprovals     :  Composition of many CERApproval on $self.ID = CERApprovals.CER_ID;
    CurrentApproval :  Association to CERApproval on $self.ID = CurrentApproval.CER_ID and $self.CurrentTATLevel = CurrentApproval.Level;
    TATUser: Association to capex.Employee on upper(TATUser.Email) = upper($self.TATUserEmail);
    TotalTATLevels :  Integer;
    CurrentTATLevel: Integer;
    WorkflowRequestId : String(50);  
    TATUserEmail         : String(300);
    CERCode   : String;
    BudgetaryID     : Integer; 
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
    virtual AgainstBudgetaryTotalCost : Double default 0.0   
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
    CER_ID: UUID;
    TATUser: Association to capex.Employee on upper(TATUser.Email) = upper($self.TATUserEmail);
    TAT: Association to capex.MasterTAT on TAT.ID = $self.TAT_ID;
    TAT_ID: Integer;
    TATUserEmail: String;
    Level: Integer;
    Status: String default 'Pending';
    TATDurationMinutes: Integer;
}

entity MediaStore : cuid, db.fingerprints {
    MediaId: String;
    MediaName: String;
    Content: LargeBinary;
    ContentType: String;
    Url: String;
    DirectoryName: String;
    DirectoryId: String;
    Status: String;
    MediaSize: Int64;
}

entity Employee :  cuid, db.fingerprints{
    FirstName   : String(30);
    LastName    : String(30);
    Email       : String(300) not null;
    Mobile      : Integer;
    Designation : String(50);
}

entity ApprovalQuery: cuid, db.fingerprints {
    Recipients: Composition of many ApprovalQueryRecipients on Recipients.ApprovalQueryID = $self.ID;
    CERApprovalId: UUID;
    Comment: String;
    MediaStoreId: UUID;
}

entity ApprovalQueryRecipients: cuid, db.fingerprints {
    ApprovalQueryID: UUID;
    Name: String;
    Email: String(300);
}

// @cds.persistence.skip
// entity ApprovalReply: cuid, db.fingerprints {
//     ApprovalQueryId: UUID;
//     ApprovalId: Int64;
//     Comment: String;
//     Attachment: LargeBinary @Core.MediaType: attachmentType @Core.ContentDisposition.Filename: attachmentName @Core.ContentDisposition.Type: 'attachment';
//     AttachmentName : String;
//     AttachmentType: String @Core.IsMediaType;
// }

type ApprovalQueryStatistics {
    TotalQueries: Int64 default 0;
    TotalAttachments: Integer default 0;
}