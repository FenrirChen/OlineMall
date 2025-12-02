let detailModal;

document.addEventListener('DOMContentLoaded', function() {
    detailModal = new bootstrap.Modal(document.getElementById('detailModal'));
    loadOrders();
});

function loadOrders() {
    fetch('/api/orders/my')
        .then(res => {
            if (res.status === 401) {
                window.location.href = '/login.html';
                return;
            }
            return res.json();
        })
        .then(orders => {
            document.getElementById('loading').style.display = 'none';
            if (orders) renderOrders(orders);
        })
        .catch(err => {
            console.error(err);
            document.getElementById('loading').innerHTML = '<span class="text-danger">加载失败</span>';
        });
}

function renderOrders(orders) {
    const tbody = document.getElementById('order-list');
    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">暂无订单记录</td></tr>';
        return;
    }

    let html = '';
    orders.forEach(order => {
        let statusBadge = '';
        switch(order.status) {
            case 'PENDING': statusBadge = '<span class="badge bg-warning text-dark">待发货</span>'; break;
            case 'SHIPPED': statusBadge = '<span class="badge bg-primary">已发货</span>'; break;
            case 'COMPLETED': statusBadge = '<span class="badge bg-success">已完成</span>'; break;
            default: statusBadge = '<span class="badge bg-secondary">未知</span>';
        }

        // 处理可能为 null 的收货人 (使用 receiverName)
        const receiver = order.receiverName ? order.receiverName : '-';

        html += `
                <tr>
                    <td>#${order.id}</td>
                    <td>${new Date(order.createTime).toLocaleString()}</td>
                    <td>${receiver}</td>
                    <td class="fw-bold">¥${order.totalAmount}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <button onclick="viewDetail(${order.id})" class="btn btn-sm btn-info text-white">查看详情</button>
                    </td>
                </tr>
            `;
    });
    tbody.innerHTML = html;
}

function viewDetail(orderId) {
    const content = document.getElementById('modal-body-content');
    content.innerHTML = '<div class="text-center"><div class="spinner-border text-primary"></div></div>';
    detailModal.show();

    fetch(`/api/orders/${orderId}`)
        .then(res => {
            if (!res.ok) throw new Error("加载详情失败");
            return res.json();
        })
        .then(data => {
            const order = data.order;
            const items = data.items;

            const receiverName = order.receiverName || '未填写';
            const address = order.address || '未填写';

            let itemsHtml = '<ul class="list-group list-group-flush">';
            items.forEach(item => {
                itemsHtml += `
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span>${item.productName}</span>
                            <span>x${item.quantity} <small class="text-muted">(¥${item.price})</small></span>
                        </li>`;
            });
            itemsHtml += '</ul>';

            content.innerHTML = `
                    <div class="mb-3 p-2 bg-light rounded">
                        <strong>收货人:</strong> ${receiverName}<br>
                        <strong>地址:</strong> ${address}
                    </div>
                    <h6>商品清单:</h6>
                    ${itemsHtml}
                    <div class="text-end mt-3 fw-bold text-danger">总计: ¥${order.totalAmount}</div>
                `;
        })
        .catch(err => {
            content.innerHTML = '<p class="text-danger text-center">无法加载详情</p>';
        });
}