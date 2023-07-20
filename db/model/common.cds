namespace capex;

using { Currency, cuid, managed, sap.common.CodeList } from '@sap/cds/common';

aspect fingerprints: managed {
    IsArchived: Boolean default false;
}