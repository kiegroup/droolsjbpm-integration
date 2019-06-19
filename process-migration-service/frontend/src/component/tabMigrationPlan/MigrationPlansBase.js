import React from "react";
import axios from "axios";

import { BACKEND_URL } from "../common/PimConstants";

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
    const servicesUrl = BACKEND_URL + "/plans";
    axios
      .get(servicesUrl, {})
      .then(res => {
        const plans = res.data;
        this.setState({ plans, filteredPlans: plans });
      })
      .catch(() => {
        this.props.onError(
          "Unable to retrieve the migration plans. Confirm the selected KIE Server is online"
        );
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
    //need to create a temp variable "self" to store this, so I can invoke this inside axios call
    const self = this;
    const serviceUrl = BACKEND_URL + "/plans/" + this.state.deletePlanId;
    axios
      .delete(serviceUrl, {
        headers: {
          "Content-Type": "application/json"
        }
      })
      .then(function() {
        self.retrieveAllPlans();
        self.hideDeleteDialog();
      })
      .catch(() => {
        this.props.onError(
          "Unable to delete the plan. Confirm the selected KIE Server is online"
        );
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

    //need to create a temp variable "self" to store this, so I can invoke this inside axios call
    const self = this;
    const servicsUrl = BACKEND_URL + "/plans";
    axios
      .post(servicsUrl, plan, {
        headers: {
          "Content-Type": "application/json"
        }
      })
      .then(function(response) {
        self.setState({
          addPlanResponseJsonStr: JSON.stringify(response.data, null, 2)
        });
        self.retrieveAllPlans();
      })
      .catch(() => {
        this.props.onError(
          "Unable to create the plan. Confirm the selected KIE Server is online"
        );
      });
  };

  editPlan = (plan, planId) => {
    //need to create a temp variable "self" to store this, so I can invoke this inside axios call
    const self = this;
    const serviceUrl = BACKEND_URL + "/plans/" + planId;
    axios
      .put(serviceUrl, plan, {
        headers: {
          "Content-Type": "application/json"
        }
      })
      .then(function(response) {
        self.setState({
          addPlanResponseJsonStr: JSON.stringify(response.data, null, 2)
        });
        self.retrieveAllPlans();
      })
      .catch(() => {
        this.props.onError(
          "Unable to update the plan. Confirm the selected KIE Server is online"
        );
      });
  };

  openMigrationWizard = rowData => {
    const servicesUrl = BACKEND_URL + "/kieserver/instances";
    axios
      .get(servicesUrl, {
        params: {
          containerId: rowData.sourceContainerId,
          kieServerId: this.props.kieServerId
        }
      })
      .then(res => {
        const instances = res.data;
        this.setState({
          runningInstances: instances,
          showMigrationWizard: true,
          planId: rowData.id
        });
        this.refs.WizardExecuteMigrationChild.resetWizardStates();
      })
      .catch(() => {
        this.props.onError(
          "Unable to open the migration wizard. Verify the selected KIE Server is online."
        );
      });
  };
}
