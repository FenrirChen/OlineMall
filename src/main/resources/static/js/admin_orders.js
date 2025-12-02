let shipModal;
let detailModal;
let currentShipId = null;

document.addEventListener('DOMContentLoaded', function() {
    shipModal = new bootstrap.Modal(document.getElementById('shipConfirmModal'));
    detailModal = new bootstrap.Modal(document.getElementById('detailModal'));
    loadOrders();
});

function showToast(msg, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    document.getElementById('toast-msg').innerText = msg;
    toastEl.className = `toast align-items-center text-white border-0 bg-${type === 'error' ? 'danger' : 'success'}`;
    new bootstrap.Toast(toastEl).show();
}

// 基础鉴权
function checkAuth(res) {
    if (res.status === 401 || res.status === 403) {
        window.location.href = '/login.html';
        throw new Error("AUTH_ERROR");
    }
    if (!res.ok) throw new Error("API Error");
    return res;
}

function loadOrders() {
    const filter = document.getElementById('statusFilter').value;
    let url = '/api/orders/all';
    if (filter) url += `?status=${filter}`;

    fetch(url)
        .then(res => checkAuth(res))
        .then(res => res.json())
        .then(orders => renderTable(orders))
        .catch(err => {
            if(err.message !== "AUTH_ERROR") showToast('加载订单列表失败', 'error');
        });
}

function renderTable(orders) {
    const tbody = document.getElementById('order-list');
    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">暂无符合条件的订单</td></tr>';
        return;
    }

    let html = '';
    orders.forEach(o => {
        let statusBadge = '';
        let shipBtn = '';

        if (o.status === 'PENDING') {
            statusBadge = '<span class="badge bg-warning text-dark">待发货</span>';
            shipBtn = `<button class="btn btn-sm btn-success" onclick="openShipModal(${o.id})">发货</button>`;
        } else if (o.status === 'SHIPPED') {
            statusBadge = '<span class="badge bg-primary">已发货</span>';
            shipBtn = '<button class="btn btn-sm btn-secondary" disabled>已发货</button>';
        } else {
            statusBadge = '<span class="badge bg-success">已完成</span>';
            shipBtn = '<button class="btn btn-sm btn-light" disabled>-</button>';
        }

        const receiver = o.receiverName ? o.receiverName : '<span class="text-muted">未知</span>';

        html += `
                <tr>
                    <td>#${o.id}</td>
                    <td>${o.userId}</td>
                    <td>${receiver}</td>
                    <td class="fw-bold">¥${o.totalAmount}</td>
                    <td>${statusBadge}</td>
                    <td class="action-btn-group">
                        ${shipBtn}
                        <button class="btn btn-sm btn-outline-info" onclick="viewDetail(${o.id})">详情</button>
                    </td>
                </tr>
            `;
    });
    tbody.innerHTML = html;
}

// 打开确认发货弹窗
function openShipModal(id) {
    currentShipId = id;
    document.getElementById('modal-ship-oid').innerText = '#' + id;
    shipModal.show();
}

// 执行发货
function executeShip() {
    if (!currentShipId) return;

    fetch(`/api/orders/${currentShipId}/ship`, { method: 'POST' })
        .then(res => checkAuth(res))
        .then(() => {
            shipModal.hide();
            showToast('发货成功');
            loadOrders(); // 刷新列表
        })
        .catch(err => {
            if(err.message !== "AUTH_ERROR") showToast('发货失败，请重试', 'error');
        });
}

// 查看详情
function viewDetail(orderId) {
    const content = document.getElementById('modal-detail-content');
    content.innerHTML = '<div class="text-center"><div class="spinner-border text-primary"></div></div>';
    detailModal.show();

    fetch(`/api/orders/${orderId}`)
        .then(res => checkAuth(res))
        .then(res => res.json())
        .then(data => {
            const order = data.order;
            const items = data.items;
            const receiverName = order.receiverName || '未填写';
            const address = order.address || '未填写';

            let itemsHtml = '<ul class="list-group mb-3">';
            items.forEach(item => {
                itemsHtml += `
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <div>
                                <strong>${item.productName}</strong>
                                <br><small class="text-muted">单价: ¥${item.price}</small>
                            </div>
                            <span class="badge bg-primary rounded-pill">x ${item.quantity}</span>
                        </li>`;
            });
            itemsHtml += '</ul>';

            content.innerHTML = `
                    <div class="mb-3 p-3 bg-light rounded">
                        <p class="mb-1"><strong>订单号：</strong> #${order.id}</p>
                        <p class="mb-1"><strong>收货人：</strong> ${receiverName}</p>
                        <p class="mb-0"><strong>地址：</strong> ${address}</p>
                    </div>
                    <h6>商品清单：</h6>
                    ${itemsHtml}
                    <div class="text-end fw-bold text-danger fs-5">总计: ¥${order.totalAmount}</div>
                `;
        })
        .catch(() => {
            content.innerHTML = '<p class="text-danger text-center">无法加载详情</p>';
        });
}