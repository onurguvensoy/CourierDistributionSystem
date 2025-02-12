<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Delivery Reports" />

<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<%@ include file="common/topbar.jsp" %>


<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
<link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">


<div class="d-sm-flex align-items-center justify-content-between mb-4">
    <h1 class="h3 mb-0 text-gray-800">Delivery Reports</h1>
    <div>
        <button class="btn btn-primary" onclick="printAllReports()">
            <i class="fas fa-print"></i> Print All Reports
        </button>
    </div>
</div>


<div class="card shadow mb-4">
    <div class="card-header py-3">
        <h6 class="m-0 font-weight-bold text-primary">All Delivery Reports</h6>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-bordered" id="reportsTable" width="100%" cellspacing="0">
                <thead>
                    <tr>
                        <th>Report ID</th>
                        <th>Package ID</th>
                        <th>Courier</th>
                        <th>Customer</th>
                        <th>Delivery Time</th>
                        <th>Distance</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${reports}" var="report">
                        <tr>
                            <td>${report.id}</td>
                            <td>${report.deliveryPackage.id}</td>
                            <td>${report.courier.username}</td>
                            <td>${report.deliveryPackage.customer.username}</td>
                            <td><custom:formatDateTime value="${report.deliveryTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td>${report.distanceTraveled} km</td>
                            <td>
                                <span class="badge badge-success">${report.status}</span>
                            </td>
                            <td>
                                <button class="btn btn-info btn-sm" onclick="viewReport('${report.id}')">
                                    <i class="fas fa-eye"></i> View
                                </button>
                                <button class="btn btn-primary btn-sm" onclick="printReport('${report.id}')">
                                    <i class="fas fa-print"></i> Print
                                </button>
                                <button class="btn btn-danger btn-sm" onclick="deleteReport('${report.id}')">
                                    <i class="fas fa-trash"></i> Delete
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>


<div class="modal fade" id="viewReportModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Report Details</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <pre id="reportContent" class="bg-light p-3" style="white-space: pre-wrap;"></pre>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" onclick="printCurrentReport()">Print Report</button>
            </div>
        </div>
    </div>
</div>

<script>
$(document).ready(function() {
    $('#reportsTable').DataTable({
        order: [[0, 'desc']]
    });
});

function viewReport(reportId) {
    $.ajax({
        url: `/api/delivery-reports/${reportId}`,
        type: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                $('#reportContent').text(response.data.content);
                $('#viewReportModal').modal('show');
            } else {
                toastr.error('Failed to load report details');
            }
        },
        error: function() {
            toastr.error('Failed to load report details');
        }
    });
}

function printReport(reportId) {
    $.ajax({
        url: `/api/delivery-reports/${reportId}`,
        type: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                const printWindow = window.open('', '', 'height=600,width=800');
                printWindow.document.write('<html><head><title>Delivery Report</title>');
                printWindow.document.write('<style>body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; }</style>');
                printWindow.document.write('</head><body>');
                printWindow.document.write('<pre>' + response.data.content + '</pre>');
                printWindow.document.write('</body></html>');
                printWindow.document.close();
                printWindow.print();
            } else {
                toastr.error('Failed to load report for printing');
            }
        },
        error: function() {
            toastr.error('Failed to load report for printing');
        }
    });
}

function deleteReport(reportId) {
    if (confirm('Are you sure you want to delete this report?')) {
        $.ajax({
            url: `/api/delivery-reports/${reportId}`,
            type: 'DELETE',
            success: function(response) {
                if (response.status === 'success') {
                    toastr.success('Report deleted successfully');
                    location.reload();
                } else {
                    toastr.error(response.message || 'Failed to delete report');
                }
            },
            error: function() {
                toastr.error('Failed to delete report');
            }
        });
    }
}

function printAllReports() {
    const table = document.getElementById('reportsTable');
    const printWindow = window.open('', '', 'height=600,width=800');
    printWindow.document.write('<html><head><title>All Delivery Reports</title>');
    printWindow.document.write('<style>');
    printWindow.document.write('body { font-family: Arial, sans-serif; }');
    printWindow.document.write('table { border-collapse: collapse; width: 100%; }');
    printWindow.document.write('th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }');
    printWindow.document.write('th { background-color: #f2f2f2; }');
    printWindow.document.write('</style></head><body>');
    printWindow.document.write('<h2>Delivery Reports Summary</h2>');
    printWindow.document.write(table.outerHTML);
    printWindow.document.write('</body></html>');
    printWindow.document.close();
    printWindow.print();
}

function printCurrentReport() {
    const content = document.getElementById('reportContent').textContent;
    const printWindow = window.open('', '', 'height=600,width=800');
    printWindow.document.write('<html><head><title>Delivery Report</title>');
    printWindow.document.write('<style>body { font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; }</style>');
    printWindow.document.write('</head><body>');
    printWindow.document.write('<pre>' + content + '</pre>');
    printWindow.document.write('</body></html>');
    printWindow.document.close();
    printWindow.print();
}
</script>

<%@ include file="common/footer.jsp" %> 