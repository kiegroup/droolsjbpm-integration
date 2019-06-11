import React from "react";
import KieServerClient from "../../clients/kieServerClient";
import PlanClient from "../../clients/planClient";

export default class MigrationPlansBase extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      plans: [],
      filteredPlans: [],
      showDeleteConfirmation: false,
      showMigrationWizard: false,
      showPlanWizard: false,
      deletePlanId: "",
      runningInstances: [],
      addPlanResponseJsonStr: ""
    };
  }

  componentDidMount() {
    this.retrieveAllPlans();
  }

  retrieveAllPlans = () => {
    PlanClient.getAll().then(plans => {
      this.setState({ plans, filteredPlans: plans });
    });
  };

  showDeleteDialog = id => {
    this.setState({
      showDeleteConfirmation: true,
      deletePlanId: id
    });
  };

  hideDeleteDialog = () => {
    this.setState({
      showDeleteConfirmation: false
    });
  };

  deletePlan = () => {
    PlanClient.delete(this.state.deletePlanId).then(() => {
      this.retrieveAllPlans();
      this.hideDeleteDialog();
    });
  };

  // addPlan need to be in the parent because it's shared between WizardAddPlan and Import Plan pop-up
  addPlan = plan => {
    if (plan !== null && plan !== "") {
      //step 1, replace all \" to "
      plan = plan.replace(/\\"/g, '"');
      //step 2, replace "{ to {
      plan = plan.replace('"{', "{");
      //step3, replace }" to }
      plan = plan.replace('}"', "}");
    }

    PlanClient.create(plan).then(response => {
      this.setState({
        addPlanResponseJsonStr: JSON.stringify(response, null, 2)
      });
      this.retrieveAllPlans();
    });
  };

  editPlan = (plan, planId) => {
    PlanClient.update(planId, plan).then(response => {
      this.setState({
        addPlanResponseJsonStr: JSON.stringify(response, null, 2)
      });
      this.retrieveAllPlans();
    });
  };

  openMigrationWizard = rowData => {
    KieServerClient.getInstances(this.props.kieServerId)
      .then(res => {
        const instances = res.data;
        this.setState({
          runningInstances: instances,
          showMigrationWizard: true,
          planId: rowData.id,
          errorMsg: ""
        });
        this.refs.WizardExecuteMigrationChild.resetWizardStates();
      })
      .catch(error => {
        this.setState({ errorMsg: error.message });
      });
  };
}
