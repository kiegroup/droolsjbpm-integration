import React, { Component } from "react";
import { Table } from "patternfly-react";
import { Icon } from "patternfly-react";
import { OverlayTrigger } from "patternfly-react";
import { Tooltip } from "patternfly-react";
import { actionHeaderCellFormatter } from "patternfly-react";

import MigrationPlansEditPopup from "./MigrationPlansEditPopup";

export default class MigrationPlansTable extends Component {
  render() {
    const headerFormat = value => <Table.Heading>{value}</Table.Heading>;
    const cellFormat = value => <Table.Cell>{value}</Table.Cell>;
    const tooltipExecute = (
      <Tooltip id="tooltip">
        <div>Execute Migration Plan</div>
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
          label: "Source Container ID",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "sourceContainerId"
      },
      {
        header: {
          label: "Target Container ID",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "targetContainerId"
      },
      {
        header: {
          label: "Target Process ID",
          formatters: [headerFormat]
        },
        cell: {
          formatters: [cellFormat]
        },
        property: "targetProcessId"
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
              <Table.Actions key="0">
                <OverlayTrigger overlay={tooltipExecute} placement={"bottom"}>
                  <Table.Button
                    bsStyle="link"
                    onClick={() => this.props.openMigrationWizard(rowData)}
                  >
                    <Icon type="fa" name="play" />
                  </Table.Button>
                </OverlayTrigger>
              </Table.Actions>,
              <Table.Actions key="1">
                <MigrationPlansEditPopup
                  title="Export Migration Plan"
                  actionName="Export"
                  buttonLabel="Copy To Clipboard"
                  content={JSON.stringify(rowData)}
                  retrieveAllPlans={this.props.retrieveAllPlans}
                  updatePlan={this.props.updatePlan}
                  planId={rowData.id}
                />
              </Table.Actions>,
              <Table.Actions key="2">
                <OverlayTrigger overlay={tooltipDelete} placement={"bottom"}>
                  <Table.Button
                    bsStyle="link"
                    onClick={() => this.props.showDeleteDialog(rowData.id)}
                  >
                    <Icon type="fa" name="trash" />
                  </Table.Button>
                </OverlayTrigger>
              </Table.Actions>,
              <Table.Actions key="3">
                <OverlayTrigger overlay={tooltipEdit} placement={"bottom"}>
                  <Table.Button
                    bsStyle="link"
                    onClick={() =>
                      this.props.openAddPlanWizardWithInitialData(rowData)
                    }
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
      <div>
        <Table.PfProvider striped bordered hover columns={planBootstrapColumns}>
          <Table.Header />
          <Table.Body rows={this.props.filteredPlans} rowKey="id" />
        </Table.PfProvider>
      </div>
    );
  }
}
