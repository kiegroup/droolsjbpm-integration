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

  savePlan = () => {
    let promise;
    if (this.state.plan.id === undefined) {
      promise = PlanClient.create(this.state.plan);
    } else {
      promise = PlanClient.update(this.state.plan.id, this.state.plan);
    }
    promise.then(response => {
      this.setState({
        plan: response
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
          plan: rowData,
          errorMsg: ""
        });
        this.refs.WizardExecuteMigrationChild.resetWizardStates();
      })
      .catch(error => {
        this.setState({ errorMsg: error.message });
      });
  };
}
