document.addEventListener('DOMContentLoaded', function() {
    checkAdmin();
    loadStatistics();
});

function showToast(msg) {
    document.getElementById('toast-msg').innerText = msg;
    new bootstrap.Toast(document.getElementById('liveToast')).show();
}

function checkAdmin() {
    const userStr = localStorage.getItem('user');
    if (!userStr || JSON.parse(userStr).role !== 'ADMIN') {
        location.href = '/login.html';
    }
}

// 加载后端真实统计数据
function loadStatistics() {
    fetch('/api/admin/stats')
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                localStorage.removeItem('user');
                location.href = '/login.html';
                throw new Error("AUTH_ERROR");
            }
            if(!res.ok) throw new Error("API Error: " + res.status);
            return res.json();
        })
        .then(data => {
            // 1. 填充顶部核心数据
            const sales = data.totalSales ? data.totalSales : 0;
            document.getElementById('total-sales').innerText = '¥' + sales.toLocaleString();
            document.getElementById('total-orders').innerText = data.totalOrders || 0;
            document.getElementById('total-users').innerText = data.totalUsers || 0;

            // 2. 填充排行榜
            renderTopProducts(data.topProducts);

            // 3. 填充销售报表 (真实数据)
            // 这里使用的是后端 statsService.getRecentSalesStats(7) 返回的 dailyReport
            renderReportTable(data.dailyReport);
        })
        .catch(err => {
            if (err.message !== "AUTH_ERROR") {
                console.error(err);
                showToast("获取数据异常: " + err.message);
            }
        });
}

function renderTopProducts(products) {
    const topList = document.getElementById('top-products');
    let listHtml = '';
    if (products && products.length > 0) {
        products.forEach((p, index) => {
            let badgeClass = index < 3 ? 'bg-danger' : 'bg-secondary';
            listHtml += `<li class="list-group-item d-flex justify-content-between align-items-center">
                                <div>
                                    <span class="badge ${badgeClass} me-2">${index + 1}</span>
                                    ${p.name}
                                </div>
                                <small class="text-muted fw-bold">销量: ${p.count}</small>
                             </li>`;
        });
    } else {
        listHtml = '<li class="list-group-item text-center text-muted">暂无销售数据</li>';
    }
    topList.innerHTML = listHtml;
}

// 渲染报表表格
function renderReportTable(dailyReport) {
    const tbody = document.getElementById('report-table-body');

    if (!dailyReport || dailyReport.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4">暂无近期数据</td></tr>';
        return;
    }

    let html = '';
    dailyReport.forEach(day => {
        // 根据是否有销量显示不同状态
        const hasSales = day.sales > 0;
        const statusBadge = hasSales
            ? '<span class="badge bg-success">已入账</span>'
            : '<span class="badge bg-light text-muted border">无交易</span>';

        // 格式化金额
        const formattedSales = '¥' + (day.sales ? day.sales.toLocaleString() : '0.00');

        html += `
                <tr>
                    <td>${day.date}</td>
                    <td>${day.orders} 单</td>
                    <td class="fw-bold ${hasSales ? 'text-primary' : 'text-muted'}">${formattedSales}</td>
                    <td>${statusBadge}</td>
                </tr>
            `;
    });
    tbody.innerHTML = html;
}

function exportReport() {
    showToast("正在生成报表并下载...");
    // 浏览器会自动识别 attachment 响应头并开始下载
    window.location.href = '/api/admin/export';
}