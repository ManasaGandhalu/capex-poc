namespace capex;
using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';

aspect fingerprints: managed {
    IsArchived: Boolean default false;
}

entity CER : fingerprints{
    key CERNumber   : Integer;
    BudgetaryID     : Integer;
    CurrentStage    : String(20) ;  
    CurrentApproverLevel :  String(30);
    WorkflowRequestId : String(50);
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

entity CERApproval : fingerprints {
    key ID: Integer;
    ApprovalLevel : String(30);
    ApprovalStatus : String(50);
    CER         : Association to CER;
}
entity MediaStore : cuid, fingerprints {
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

entity ApprovalQuery: cuid, fingerprints {
    approvalId: Int64;
    comment: String;
    mediaStoreId: UUID;
}

// @cds.persistence.skip
// entity ApprovalReply: cuid, fingerprints {
//     approvalQueryId: UUID;
//     approvalId: Int64;
//     comment: String;
//     attachment: LargeBinary @Core.MediaType: attachmentType @Core.ContentDisposition.Filename: attachmentName @Core.ContentDisposition.Type: 'attachment';
//     attachmentName : String;
//     attachmentType: String @Core.IsMediaType;
// }
