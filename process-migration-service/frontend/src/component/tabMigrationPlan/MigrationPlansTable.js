import React from "react";
import PropTypes from "prop-types";
import {
  Table,
  Icon,
  OverlayTrigger,
  Tooltip,
  actionHeaderCellFormatter
} from "patternfly-react";

import WizardExecuteMigration from "./wizardExecuteMigration/WizardExecuteMigration";
import planClient from "../../clients/planClient";
import ExportPlanModal from "./ExportPlanModal";

export default class MigrationPlansTable extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showMigrationWizard: false,
      containerId: ""
    };
  }

  openMigrationWizard = id => {
    planClient.get(id).then(plan => {
      this.setState({
        showMigrationWizard: true,
        containerId: plan.source.containerId,
        planId: id
      });
    });
  };

  render() {
    const headerFormat = value => <Table.Heading>{value}</Table.Heading>;
    const cellFormat = (value, def) => {
      const parts = def.property.split(".");
      let newVal = def.rowData;
      parts.forEach(part => {
        newVal = newVal[part];
      });
      return <Table.Cell>{newVal}</Table.Cell>;
    };

    const tooltipExecute = (
      <Tooltip id="tooltip">
        <div>Execute migration</div>
      </Tooltip>
    );

    const tooltipDelete = (
      <Tooltip id="tooltip">
        <div>Delete</div>
      </Tooltip>
    );
    const tooltipEdit = (
      <Tooltip id="tooltip">
        <div>Edit</div>
      </Tooltip>
    );

    const planBootstrapColumns = [
      {
        header: {
          label: "ID",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "id"
      },
      {
        header: {
          label: "Plan Name",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "name"
      },
      {
        header: {
          label: "Description",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "description"
      },
      {
        header: {
          label: "Source container",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "source.containerId"
      },
      {
        header: {
          label: "Source process",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "source.processId"
      },
      {
        header: {
          label: "Target container",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "target.containerId"
      },
      {
        header: {
          label: "Target process",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "target.processId"
      },
      {
        header: {
          label: "Actions",
          props: {
            rowSpan: 1,
            colSpan: 4
          },
          formatters: [actionHeaderCellFormatter]
        },
        cell: {
          formatters: [
            (value, { rowData }) => [
              <Table.Actions key="Actions_Migration">
                <OverlayTrigger overlay={tooltipExecute} placement={"bottom"}>
                  <Table.Button
                    bsStyle="link"
                    onClick={() => this.openMigrationWizard(rowData.id)}
                  >
                    <Icon type="fa" name="play" />
                  </Table.Button>
                </OverlayTrigger>
              </Table.Actions>,
              <Table.Actions key="Actions_Export">
                <ExportPlanModal plan={rowData} />
              </Table.Actions>,
              <Table.Actions key="Actions_Delete">
                <OverlayTrigger overlay={tooltipDelete} placement={"bottom"}>
                  <Table.Button
                    bsStyle="link"
                    onClick={() => this.props.onDeletePlan(rowData)}
                  >
                    <Icon type="fa" name="trash" />
                  </Table.Button>
                </OverlayTrigger>
              </Table.Actions>,
              <Table.Actions key="Actions_Edit">
                <OverlayTrigger overlay={tooltipEdit} placement={"bottom"}>
                  <Table.Button
                    bsStyle="link"
                    onClick={() => this.props.onEditPlan(rowData.id)}
                  >
                    <Icon type="fa" name="edit" />
                  </Table.Button>
                </OverlayTrigger>
              </Table.Actions>
            ]
          ]
        },
        property: "action"
      }
    ];

    return (
      <React.Fragment>
        <Table.PfProvider striped hover columns={planBootstrapColumns}>
          <Table.Header />
          <Table.Body rows={this.props.plans} rowKey="id" />
        </Table.PfProvider>
        {this.state.showMigrationWizard && (
          <WizardExecuteMigration
            isOpen={this.state.showMigrationWizard}
            onClose={() => this.setState({ showMigrationWizard: false })}
            containerId={this.state.containerId}
            kieServerId={this.props.kieServerId}
            planId={this.state.planId}
          />
        )}
      </React.Fragment>
    );
  }
}

MigrationPlansTable.propTypes = {
  plans: PropTypes.array.isRequired,
  kieServerId: PropTypes.string.isRequired,
  onEditPlan: PropTypes.func.isRequired,
  onDeletePlan: PropTypes.func.isRequired
};
