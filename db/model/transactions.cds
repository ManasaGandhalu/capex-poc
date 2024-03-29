namespace capex;

using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';
using { capex as db} from '../schema';

entity CER : cuid, db.fingerprints {
    CERType         : Association to db.MasterCERType on $self.CERType_ID = CERType.ID; 
    Status          : Association to db.MasterStatus on $self.Status_ID = Status.ID; 
    CurrentStage    : Association to db.MasterStage on $self.CurrentStage_ID = CurrentStage.ID; 
    ParentCER       : Association to CER;  
    CERLineItems     : Composition of many CERLineItem on $self.ID = CERLineItems.CER_ID;
    CERApprovals     :  Composition of many CERApproval on $self.ID = CERApprovals.CER_ID;
    CurrentApproval :  Association to CERApproval on $self.ID = CurrentApproval.CER_ID and $self.CurrentTATLevel = CurrentApproval.Level;
    TATUser: Association to db.Employee on upper(TATUser.Email) = upper($self.TATUserEmail);
    CreatedByUser: Association to db.Employee on upper(CreatedByUser.Email) = upper($self.createdBy);
    TotalTATLevels :  Integer;
    CurrentTATLevel: Integer;
    CERType_ID: Integer;
    Status_ID: Integer;
    CurrentStage_ID: Integer;
    WorkflowRequestId : String;  
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
    MediaStoreId   : UUID;
    virtual AgainstBudgetaryTotalCost : Double default 0.0;
    virtual AgainstBudgetaryCount: Int64;
    virtual  Attachment: LargeBinary;
    virtual  AttachmentName : String;
    virtual AttachmentType: String;
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
    TATUser: Association to db.Employee on upper(TATUser.Email) = upper($self.TATUserEmail);
    TAT: Association to db.MasterTAT on TAT.ID = $self.TAT_ID;
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
    Recipients: Composition of many db.ApprovalQueryRecipients on Recipients.ApprovalQueryID = $self.ID;
    Sender: Association to db.Employee on upper(Sender.Email) = upper($self.createdBy);
    CERApprovalId: UUID;
    Comment: String;
    MediaStoreId: UUID;
}

entity ApprovalQueryRecipients: cuid, db.fingerprints {
    ApprovalQueryID: UUID;
    Name: String;
    Email: String(300);
}

type ApprovalQueryStatistics {
    TotalQueries: Int64 default 0;
    TotalAttachments: Integer default 0;
}

type AgainstBudgetaryStatistics {
    AgainstBudgetaryCount: Int64 default 0;
    AgainstBudgetaryTotalCost: Double default 0.0;
}