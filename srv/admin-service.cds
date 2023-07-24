using { capex as db } from '../db/schema';

service AdminService {
    entity MasterCERType as projection on db.MasterCERType;
    entity MasterStage as projection on db.MasterStage;
    entity MasterStatus as projection on db.MasterStatus;
    entity MasterAmount as projection on db.MasterAmount;
    entity MasterAssetType as projection on db.MasterAssetType;
    entity MasterExpenseType as projection on db.MasterExpenseType;
    entity MasterProjectedReturn as projection on db.MasterProjectedReturn;
    entity MasterTAT as projection on db.MasterTAT;
    entity MasterTATLevel as projection on db.MasterTATLevel;
    entity MasterRecurringExpense as projection on db.MasterRecurringExpense;
    entity MediaStore as projection on db.MediaStore;
    entity MasterCostCenter as projection on db.MasterCostCenter;
    entity MasterProfitCenter as projection on db.MasterProfitCenter;
    entity Employee as projection on db.Employee;
    entity CER as projection on db.CER;
    entity CERApproval as projection on db.CERApproval {
        *,
        virtual 0 as TotalQueries: Int64,
        virtual 0 as TotalAttachments: Integer
    };
    
    entity ApprovalQuery as projection on db.ApprovalQuery {
        *,

        @Core.Computed: false
        virtual null as Attachment: LargeBinary,

        @Core.Computed: false
        virtual null as AttachmentName : String,
        
        @Core.Computed: false
        virtual null as AttachmentType: String
    };

    entity ApprovalQueryRecipients as projection on db.ApprovalQueryRecipients;

    function UpdateApprovalStatus(CerApprovalId: UUID, Status: String) returns CERApproval;

    function ChangeApprovalStatusByWorkflow(CerApprovalId: UUID, Status: String) returns CERApproval;
    
}
