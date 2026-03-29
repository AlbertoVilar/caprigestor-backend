# User Manual — CapriGestor / GoatFarm

Last update: 2026-03-29  
Scope: official functional guide for day-to-day farm operation.  
Language: English.  
Related links: [Portal](../INDEX.md), [Business Domain](./BUSINESS_DOMAIN.md), [Homologation Runbook](./HOMOLOGATION_OPERATION_RUNBOOK.md), [Freeze and Pilot Playbook](./PILOT_FREEZE_PLAYBOOK.md)

## 1. System overview

CapriGestor / GoatFarm is a farm management system focused on real-world goat farming operations.

It centralizes:

- farm and herd registration;
- animal genealogy and history;
- reproduction, pregnancy, birth, and weaning;
- lactation, milk production, and dry-off;
- health events and withdrawal periods;
- inventory;
- commercial routines;
- minimum operational finance.

The system prioritizes:

- operational clarity;
- traceability;
- farm-level security;
- practical daily use.

## 2. User roles

The most relevant operational roles are:

- `Administrator`: full access to allowed operations.
- `Operator`: performs daily operational routines according to farm permissions.
- `Farm owner`: manages the farm under their ownership scope.

Some actions are restricted by permissions and farm ownership rules.

## 3. Accessing the system

### 3.1 Login

Use the login screen to access the platform with email and password.

Basic flow:

1. enter your email;
2. enter your password;
3. sign in;
4. access the assigned farm.

### 3.2 Password reset

The system includes an email-based password reset flow.

Flow:

1. click `Forgot password`;
2. enter the registered email;
3. open the link received by email;
4. define a new password.

Note:

- the request response is neutral and does not reveal whether the email exists.

## 4. Main navigation

The main platform areas are:

- `Home`;
- `Farms`;
- `Goats`;
- `Blog`;
- internal operational pages by farm.

Inside a farm, the most important areas are:

- farm dashboard;
- herd;
- animal detail;
- reproduction;
- lactation;
- milk production;
- health;
- inventory;
- commercial.

## 5. Farm and herd

### 5.1 Farm registration

The system allows users to:

- create a farm;
- update farm data;
- check farm permissions;
- maintain operational farm information.

### 5.2 Goat registration

The system allows users to:

- register goats manually;
- list goats by farm;
- search by name;
- update animal data;
- delete when allowed;
- register controlled animal exit.

### 5.3 ABCC import

When applicable, the system allows users to:

- load ABCC breed options;
- search public ABCC animals;
- preview import data;
- confirm single import;
- confirm batch import.

### 5.4 Controlled animal exit

Animal exit is traceable and does not remove historical data.

Main exit types:

- sale;
- death;
- discard;
- donation;
- transfer.

The system stores:

- exit type;
- exit date;
- notes.

## 6. Animal detail

The animal detail page is one of the most important operational screens.

It usually shows:

- animal identity;
- current status;
- zootechnical data;
- genealogy;
- operational history;
- quick actions;
- access to reproduction, lactation, health, and milk routes.

Use this screen as the central reference point for the animal history.

## 7. Reproduction

The reproduction module covers the reproductive cycle.

### 7.1 Breeding

Users can register breeding events.

Important rules:

- a new breeding event should not be used to simulate a correction of a previous breeding event;
- the system always uses the latest valid breeding event as the active cycle reference;
- inconsistent breeding records may be blocked.

### 7.2 Pregnancy diagnosis

The system allows positive or negative pregnancy diagnosis.

Important rule:

- positive confirmation is only allowed from 60 days after the valid breeding event used as reference.

### 7.3 Active pregnancy and history

The reproduction screen shows:

- active pregnancy, when it exists;
- pregnancy history;
- recent reproductive events;
- diagnosis recommendation;
- farm-level alerts for pending diagnosis.

### 7.4 Closing a pregnancy

A pregnancy can be closed using a valid reason, for example:

- birth;
- abortion;
- loss;
- false positive;
- another supported operational reason.

### 7.5 Birth

The system allows users to register birth and create the offspring records.

### 7.6 Weaning

The system allows weaning registration when the animal is eligible.

## 8. Lactation

The lactation module controls the production cycle.

### 8.1 Starting lactation

Start lactation when the goat begins milk production.

### 8.2 Active lactation

When lactation is active, the system shows:

- start date;
- current status;
- quick actions;
- cycle summary;
- dry-off recommendation when there is an active confirmed pregnancy.

### 8.3 Dry-off

Dry-off is a key operational step.

Main rules:

- confirmed dry-off interrupts production for that cycle;
- dry-off does not necessarily mean a definitive cycle closure;
- dry-off moves the lactation to `DRY` state.

### 8.4 Resuming lactation

Resuming is allowed only in coherent scenarios.

Typical examples:

- false positive pregnancy;
- abortion;
- pregnancy loss.

Important rule:

- if pregnancy ended in birth, the correct action is to start a new lactation, not resume the old one.

### 8.5 Business restrictions linked to pregnancy

With an active pregnancy after confirmed dry-off:

- a new lactation cannot be started;
- the dried lactation cannot be resumed;
- the same production cycle cannot be resumed while the rule remains active.

## 9. Milk production

The milk production module stores milking records by goat.

### 9.1 Recording production

Users can register milk production by:

- date;
- shift;
- volume;
- note.

The system also allows users to:

- view paginated history;
- update allowed fields;
- logically cancel a record when needed.

### 9.2 Uniqueness rule

The system protects against duplicate records for the same date and milking shift.

### 9.3 Production during sanitary withdrawal

If there is an active milk withdrawal period:

- the system still allows recording the real production;
- the record is flagged as produced during withdrawal;
- this preserves the real zootechnical history of the animal.

Stored traceability fields include:

- withdrawal production flag;
- source health event;
- withdrawal end date;
- summarized source treatment.

## 10. Health and veterinary

The health module supports the sanitary management of the herd.

### 10.1 Health event types

Examples include:

- vaccine;
- deworming;
- medication;
- procedure;
- disease or occurrence.

### 10.2 Event states

An event may be:

- `Scheduled`;
- `Done`;
- `Canceled`.

### 10.3 Available operations

The system allows users to:

- create health events;
- edit events;
- mark events as done;
- cancel events;
- reopen events when allowed;
- view goat-level history;
- view the farm health calendar;
- view farm-level health alerts.

## 11. Operational withdrawal periods

Withdrawal periods are treated as live operational information.

### 11.1 What the system calculates

From a completed health event, the system can determine:

- whether there is an active milk withdrawal period;
- whether there is an active meat withdrawal period;
- when the withdrawal ends;
- which treatment caused the restriction.

### 11.2 Where it appears

Withdrawal information appears in:

- health event detail;
- goat operational status;
- farm agenda and alerts;
- milk production screens.

### 11.3 Practical rule for milk

During a milk withdrawal period:

- the system shows a strong warning;
- production can still be recorded to preserve the real history;
- milk remains operationally or commercially restricted until the withdrawal ends.

### 11.4 Practical rule for meat

During a meat withdrawal period:

- the system shows a strong warning on the goat and farm screens;
- the current stage focuses on clear alerting without opening a larger parallel commercial workflow.

## 12. Farm dashboard and alerts

The farm dashboard works as the operational hub.

It concentrates, depending on the farm context:

- key indicators;
- operational agenda;
- reproduction alerts;
- dry-off alerts;
- health alerts;
- shortcuts to the main modules.

Alerts help the user find what requires action quickly.

## 13. Inventory

The inventory module controls entries, exits, adjustments, lots, and balances.

### 13.1 Items and lots

The system allows users to:

- create stock items;
- create lots;
- activate or deactivate lots;
- view lots and balances.

### 13.2 Movements

Main movement types:

- entry (`IN`);
- exit (`OUT`);
- adjustment (`ADJUST`).

Important rules:

- quantity must be greater than zero;
- negative balance is not allowed;
- lot-tracked items require a valid active lot;
- movements remain immutable after creation.

### 13.3 Purchase cost on stock entry

For purchase-based entries, the system can store:

- unit cost;
- total cost;
- purchase date;
- supplier;
- note or reason.

This cost is used in operational finance, without turning the product into a full accounting ERP.

## 14. Commercial

The commercial module covers the minimum commercial layer of the farm.

### 14.1 Customers

Users can:

- create customers;
- list customers for the farm.

### 14.2 Animal sales

Users can:

- register animal sales;
- list sales;
- record sale payments.

### 14.3 Milk sales

Users can:

- register milk sales;
- list milk sales;
- record payments.

### 14.4 Minimum receivables

The system keeps a simple receivable view derived from sales, with minimal states such as:

- `OPEN`;
- `PAID`.

## 15. Minimum operational finance

This layer answers simple monthly operational questions.

### 15.1 Operational expenses

Users can register expenses such as:

- energy;
- water;
- freight;
- maintenance;
- veterinary service;
- fuel;
- labor;
- fees;
- other expenses.

### 15.2 Monthly summary

The monthly summary shows:

- how much came in during the month;
- how much went out during the month;
- the operational balance;
- the main revenue and expense breakdown.

Sources considered in the current stage:

- received sales;
- operational expenses;
- stock purchases with registered cost.

## 16. Recommended operating practices

Recommended daily practices:

- keep animal status updated;
- register breeding and diagnosis in the real sequence of events;
- use dry-off only at the correct stage of management;
- record milk production daily instead of grouping multiple days into one record;
- mark health events as done as soon as they are performed;
- register purchase cost during stock entry whenever it is a real acquisition;
- register operational expenses in the same month they occur;
- review dashboard and alerts before starting the daily routine.

## 17. Deliberate scope limits of this stage

At this stage, the system is not intended to be:

- a full ERP;
- a formal accounting system;
- a tax platform;
- an advanced BI platform;
- a broad regulatory compliance engine.

The current focus is reliable operational farm management.

## 18. Quick troubleshooting

### If you cannot access a farm

Check:

- whether the user belongs to the farm;
- whether permissions are correct;
- whether authentication is valid.

### If you cannot record milk production

Check:

- whether there is an active lactation;
- whether the animal status is `ACTIVE`;
- whether a record already exists for the same date and shift.

### If you cannot start or resume lactation

Check:

- whether there is an active pregnancy;
- whether the goat is currently dry after confirmed dry-off;
- whether the last pregnancy ended with birth.

### If you cannot move forward in reproduction

Check:

- whether the breeding event was registered correctly;
- whether the minimum diagnosis window has already passed;
- whether the active pregnancy is still open or already closed.

## 19. Conclusion

CapriGestor / GoatFarm should be used as the farm’s operational system, with emphasis on clarity, traceability, and disciplined record keeping.

The best way to get value from the system is to:

- record events when they actually happen;
- use farm alerts as a daily reference;
- keep reproduction, lactation, health, inventory, and commercial routines aligned with real operation.
