import React from "react";

import { orderBy } from "lodash";
import classNames from "classnames";
import * as sort from "sortabular";
import * as resolve from "table-resolver";
import { compose } from "recompose";

import {
  customHeaderFormattersDefinition,
  defaultSortingOrder,
  selectionCellFormatter,
  selectionHeaderCellFormatter,
  sortableHeaderCellFormatter,
  tableCellFormatter,
  Table,
  TABLE_SORT_DIRECTION
} from "patternfly-react";
import { Grid } from "patternfly-react";
import { PaginationRow, paginate, PAGINATION_VIEW } from "patternfly-react";

/**
 * Reactabular client side data sorting based on the following api docs:
 * https://reactabular.js.org/#/data/sorting
 */

export default class PageMigrationRunningInstances extends React.Component {
  static deselectRow(row) {
    return Object.assign({}, row, { selected: false });
  }
  static selectRow(row) {
    return Object.assign({}, row, { selected: true });
  }
  constructor(props) {
    super(props);

    const getSortingColumns = () => this.state.sortingColumns || {};

    const sortableTransform = sort.sort({
      getSortingColumns,
      onSort: selectedColumn => {
        this.setState({
          sortingColumns: sort.byColumn({
            sortingColumns: this.state.sortingColumns,
            sortingOrder: defaultSortingOrder,
            selectedColumn
          })
        });
      },
      // Use property or index dependening on the sortingColumns structure specified
      strategy: sort.strategies.byProperty
    });

    const sortingFormatter = sort.header({
      sortableTransform,
      getSortingColumns,
      strategy: sort.strategies.byProperty
    });

    // enables our custom header formatters extensions to reactabular
    this.customHeaderFormatters = customHeaderFormattersDefinition;
    this.state = {
      // Sort the first column in an ascending way by default.
      sortingColumns: {
        name: {
          direction: TABLE_SORT_DIRECTION.ASC,
          position: 0
        }
      },

      // column definitions
      columns: [
        {
          property: "select",
          header: {
            label: "Select all rows",
            props: {
              index: 0,
              rowSpan: 1,
              colSpan: 1,
              id: "SelectId"
            },
            customFormatters: [selectionHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 0
            },
            formatters: [
              (value, { rowData, rowIndex }) =>
                selectionCellFormatter(
                  { rowData, rowIndex },
                  this.onSelectRow,
                  `${rowIndex}`,
                  `${rowIndex}`
                )
            ]
          }
        },
        {
          property: "processInstanceId",
          header: {
            label: "Process instance ID",
            props: {
              index: 1,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [sortableTransform],
            formatters: [sortingFormatter],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 1
            },
            formatters: [tableCellFormatter]
          }
        },
        {
          property: "name",
          header: {
            label: "Name",
            props: {
              index: 2,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [sortableTransform],
            formatters: [sortingFormatter],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 2
            },
            formatters: [tableCellFormatter]
          }
        },
        {
          property: "description",
          header: {
            label: "Description",
            props: {
              index: 3,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [sortableTransform],
            formatters: [sortingFormatter],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 3
            },
            formatters: [tableCellFormatter]
          }
        },
        {
          property: "startTime",
          header: {
            label: "Start Time",
            props: {
              index: 4,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [sortableTransform],
            formatters: [sortingFormatter],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 4
            },
            formatters: [tableCellFormatter]
          }
        },
        {
          property: "state",
          header: {
            label: "State",
            props: {
              index: 5,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [sortableTransform],
            formatters: [sortingFormatter],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 5
            },
            formatters: [tableCellFormatter]
          }
        }
      ],

      // rows and row selection state
      rows: this.props.runningInstances,
      selectedRows: [],

      // pagination default states
      pagination: {
        page: 1,
        perPage: 6,
        perPageOptions: [6, 10, 15]
      },

      // page input value
      pageChangeValue: 1
    };
  }
  onFirstPage = () => {
    this.setPage(1);
  };
  onLastPage = () => {
    const { page } = this.state.pagination;
    const totalPages = this.totalPages();
    if (page < totalPages) {
      this.setPage(totalPages);
    }
  };
  onNextPage = () => {
    const { page } = this.state.pagination;
    if (page < this.totalPages()) {
      this.setPage(this.state.pagination.page + 1);
    }
  };
  onPageInput = e => {
    this.setState({ pageChangeValue: e.target.value });
  };
  onPerPageSelect = eventKey => {
    const newPaginationState = Object.assign({}, this.state.pagination);
    newPaginationState.perPage = eventKey;
    newPaginationState.page = 1;
    this.setState({ pagination: newPaginationState });
  };
  onPreviousPage = () => {
    if (this.state.pagination.page > 1) {
      this.setPage(this.state.pagination.page - 1);
    }
  };
  onRow = row => {
    const { selectedRows } = this.state;
    const selected = selectedRows.indexOf(row.id) > -1;
    return {
      className: classNames({ selected }),
      role: "row"
    };
  };
  onSelectAllRows = event => {
    const { rows, selectedRows } = this.state;
    const { checked } = event.target;
    const currentRows = this.currentRows().rows;

    if (checked) {
      const updatedSelections = [
        ...new Set([...currentRows.map(r => r.id), ...selectedRows])
      ];
      const updatedRows = rows.map(r =>
        updatedSelections.indexOf(r.id) > -1
          ? PageMigrationRunningInstances.selectRow(r)
          : r
      );
      this.setState({
        // important: you must update rows to force a re-render and trigger onRow hook
        rows: updatedRows,
        selectedRows: updatedSelections
      });
      this.updateSelectedProcessIds(rows, updatedSelections);
    } else {
      const ids = currentRows.map(r => r.id);
      const updatedSelections = selectedRows.filter(
        r => !(ids.indexOf(r) > -1)
      );
      const updatedRows = rows.map(r =>
        updatedSelections.indexOf(r.id) > -1
          ? r
          : PageMigrationRunningInstances.deselectRow(r)
      );
      this.setState({
        rows: updatedRows,
        selectedRows: updatedSelections
      });

      this.updateSelectedProcessIds(rows, updatedSelections);
    }
  };

  updateSelectedProcessIds = (rows, updatedSelectedRows) => {
    var i;
    var ids = "";
    for (i = 0; i < updatedSelectedRows.length; i++) {
      const rowId = updatedSelectedRows[i];
      if (ids === "") {
        ids = "" + rows[rowId - 1].processInstanceId;
      } else {
        ids = ids + "," + rows[rowId - 1].processInstanceId;
      }
    }
    this.props.setRunngingInstancesIds(ids);
  };

  onSelectRow = (event, row) => {
    const { rows, selectedRows } = this.state;
    const selectedRowIndex = rows.findIndex(r => r.id === row.id);
    if (selectedRowIndex > -1) {
      let updatedSelectedRows;
      let updatedRow;
      if (row.selected) {
        updatedSelectedRows = selectedRows.filter(r => !(r === row.id));
        updatedRow = PageMigrationRunningInstances.deselectRow(row);
      } else {
        selectedRows.push(row.id);
        updatedSelectedRows = selectedRows;
        updatedRow = PageMigrationRunningInstances.selectRow(row);
      }
      rows[selectedRowIndex] = updatedRow;
      this.setState({
        rows,
        selectedRows: updatedSelectedRows
      });
      this.updateSelectedProcessIds(rows, updatedSelectedRows);
    }
  };

  onSubmit = () => {
    this.setPage(this.state.pageChangeValue);
  };
  setPage = value => {
    const page = Number(value);
    if (
      !Number.isNaN(value) &&
      value !== "" &&
      page > 0 &&
      page <= this.totalPages()
    ) {
      const newPaginationState = Object.assign({}, this.state.pagination);
      newPaginationState.page = page;
      this.setState({ pagination: newPaginationState, pageChangeValue: page });
    }
  };
  currentRows() {
    const { rows, sortingColumns, columns, pagination } = this.state;
    return compose(
      paginate(pagination),
      sort.sorter({
        columns,
        sortingColumns,
        sort: orderBy,
        strategy: sort.strategies.byProperty
      })
    )(rows);
  }
  totalPages = () => {
    const { perPage } = this.state.pagination;
    return Math.ceil(this.props.runningInstances.length / perPage);
  };
  render() {
    const { columns, pagination, sortingColumns, pageChangeValue } = this.state;
    const sortedPaginatedRows = this.currentRows();

    return (
      <Grid fluid>
        <Table.PfProvider
          striped
          bordered
          hover
          dataTable
          columns={columns}
          components={{
            header: {
              cell: cellProps =>
                this.customHeaderFormatters({
                  cellProps,
                  columns,
                  sortingColumns,
                  rows: sortedPaginatedRows.rows,
                  onSelectAllRows: this.onSelectAllRows
                })
            }
          }}
        >
          <Table.Header headerRows={resolve.headerRows({ columns })} />
          <Table.Body
            rows={sortedPaginatedRows.rows}
            rowKey="processInstanceId"
            onRow={this.onRow}
          />
        </Table.PfProvider>
        <PaginationRow
          viewType={PAGINATION_VIEW.TABLE}
          pagination={pagination}
          pageInputValue={pageChangeValue}
          amountOfPages={sortedPaginatedRows.amountOfPages}
          itemCount={sortedPaginatedRows.itemCount}
          itemsStart={sortedPaginatedRows.itemsStart}
          itemsEnd={sortedPaginatedRows.itemsEnd}
          onPerPageSelect={this.onPerPageSelect}
          onFirstPage={this.onFirstPage}
          onPreviousPage={this.onPreviousPage}
          onPageInput={this.onPageInput}
          onNextPage={this.onNextPage}
          onLastPage={this.onLastPage}
          onSubmit={this.onSubmit}
        />
      </Grid>
    );
  }
}
