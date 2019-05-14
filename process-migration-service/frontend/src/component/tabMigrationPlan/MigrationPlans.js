import React from "react";

import { Button } from "patternfly-react";
import { Icon } from "patternfly-react";
import { MessageDialog } from "patternfly-react";

import MigrationPlansBase from "./MigrationPlansBase";
import MigrationPlansTable from "./MigrationPlansTable";
import MigrationPlanListFilter from "./MigrationPlanListFilter";
import MigrationPlansEditPopup from "./MigrationPlansEditPopup";

import WizardAddPlan from "./wizardAddPlan/WizardAddPlan";
import WizardExecuteMigration from "./wizardExecuteMigration/WizardExecuteMigration";

import { AddPlanItems } from "../common/WizardItems";
import { ExecuteMigrationItems } from "../common/WizardItems";

export default class MigrationPlans extends MigrationPlansBase {
  resetAllStates = () => {
    //clean all states before open add plan wizard, otherwise the wizard-form might have last add-plan's values and steps
    this.setState({
      showDeleteConfirmation: false,
      showMigrationWizard: false,
      showPlanWizard: false,
      deletePlanId: "",
      runningInstances: [],
      planId: "",
      addPlanResponseJsonStr: ""
    });
  };

  closeMigrationWizard = () => {
    this.setState({ showMigrationWizard: false });
  };

  openAddPlanWizard = () => {
    this.resetAllStates();
    this.setState({ showPlanWizard: true });
    this.refs.WizardAddPlanChild.resetWizardStates();
  };

  openAddPlanWizardWithInitialData = rowData => {
    this.resetAllStates();
    this.setState({ showPlanWizard: true });
    this.refs.WizardAddPlanChild.initialWizardStates(rowData);
  };

  closeAddPlanWizard = () => {
    this.setState({ showPlanWizard: false });
    this.retrieveAllPlans();
  };

  onFilterChange = planFilter => {
    let filteredPlans = this.state.plans;
    filteredPlans = filteredPlans.filter(plan => {
      let sourceContainterId = plan.sourceContainerId.toLowerCase();
      return sourceContainterId.indexOf(planFilter.toLowerCase()) !== -1;
    });
    this.setState({
      filteredPlans
    });
  };

  render() {
    const { showPlanWizard, showMigrationWizard } = this.state;

    //for MessageDialogDeleteConfirmation
    const primaryContent = (
      <p className="lead">
        Please confirm you will delete this migration plan{" "}
        {this.state.deletePlanId}
      </p>
    );
    const secondaryContent = <p />;
    const icon = <Icon type="pf" name="error-circle-o" />;

    return (
      <div>
        {/* Delete Plan pop-up */}
        <MessageDialog
          show={this.state.showDeleteConfirmation}
          onHide={this.hideDeleteDialog}
          primaryAction={this.deletePlan}
          secondaryAction={this.hideDeleteDialog}
          primaryActionButtonContent="Delete"
          secondaryActionButtonContent="Cancel"
          primaryActionButtonBsStyle="danger"
          title="Delete Migration Plan"
          icon={icon}
          primaryContent={primaryContent}
          secondaryContent={secondaryContent}
          accessibleName="deleteConfirmationDialog"
          accessibleDescription="deleteConfirmationDialogContent"
        />
        <br />
        {/* import plan & Add Plan */}
        <div className="row">
          <div className="col-xs-9">
            <MigrationPlanListFilter onChange={this.onFilterChange} />
          </div>
          <div className="col-xs-3">
            <div className="pull-right">
              <MigrationPlansEditPopup
                title="Import Migration Plan"
                actionName="Import Plan"
                retrieveAllPlans={this.retrieveAllPlans}
                addPlan={this.addPlan}
              />
              &nbsp;
              <Button bsStyle="primary" onClick={this.openAddPlanWizard}>
                Add Plan
              </Button>
            </div>
          </div>
        </div>
        <br />
        {/* Table lists all the migration plans */}
        <MigrationPlansTable
          openMigrationWizard={this.openMigrationWizard}
          openAddPlanWizard={this.openAddPlanWizard}
          openAddPlanWizardWithInitialData={
            this.openAddPlanWizardWithInitialData
          }
          showDeleteDialog={this.showDeleteDialog}
          filteredPlans={this.state.filteredPlans}
          updatePlan={this.editPlan}
          retrieveAllPlans={this.retrieveAllPlans}
        />

        {/* Add Plan Wizard */}
        <WizardAddPlan
          showPlanWizard={showPlanWizard}
          closeAddPlanWizard={this.closeAddPlanWizard}
          addPlan={this.addPlan}
          editPlan={this.editPlan}
          steps={AddPlanItems}
          ref="WizardAddPlanChild"
          addPlanResponseJsonStr={this.state.addPlanResponseJsonStr}
          kieServerIds={this.props.kieServerIds}
        />

        {/* Execute Migration Wizard*/}
        <WizardExecuteMigration
          showMigrationWizard={showMigrationWizard}
          closeMigrationWizard={this.closeMigrationWizard}
          runningInstances={this.state.runningInstances}
          planId={this.state.planId}
          steps={ExecuteMigrationItems}
          ref="WizardExecuteMigrationChild"
          kieServerIds={this.props.kieServerIds}
        />
      </div>
    );
  }
}
