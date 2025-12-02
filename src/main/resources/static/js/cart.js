let currentCartItems = [];
let deleteItemId = null;
let deleteModal = null;
let checkoutModal = null;

document.addEventListener('DOMContentLoaded', () => {
    deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    checkoutModal = new bootstrap.Modal(document.getElementById('checkoutModal'));
    loadCart();
});

function showToast(msg, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    document.getElementById('toast-msg').innerText = msg;
    toastEl.className = `toast align-items-center text-white border-0 bg-${type === 'error' ? 'danger' : 'success'}`;
    new bootstrap.Toast(toastEl).show();
}

function loadCart() {
    fetch('/api/cart')
        .then(res => {
            if (res.status === 401) {
                location.href = '/login.html';
                return;
            }
            return res.json();
        })
        .then(items => {
            if(items) {
                currentCartItems = items;
                renderCart(items);
            }
        })
        .catch(err => console.error('Error:', err));
}

function renderCart(items) {
    const tbody = document.getElementById('cart-items');
    const emptyMsg = document.getElementById('empty-cart-msg');
    const checkoutBtn = document.getElementById('checkout-btn');
    let total = 0;
    let html = '';

    if (items.length === 0) {
        tbody.innerHTML = '';
        emptyMsg.classList.remove('d-none');
        checkoutBtn.disabled = true;
        document.getElementById('total-price').innerText = '¥0.00';
        return;
    }

    emptyMsg.classList.add('d-none');
    checkoutBtn.disabled = false;

    items.forEach(item => {
        const subtotal = item.price * item.quantity;
        total += subtotal;

        //数量加减控件
        html += `
                <tr>
                    <td>
                        <div class="d-flex align-items-center">
                            <img src="${item.productImage || 'https://via.placeholder.com/50'}" class="rounded me-2">
                            <span>${item.productName}</span>
                        </div>
                    </td>
                    <td>¥${item.price}</td>
                    <td>
                        <div class="input-group input-group-sm">
                            <button class="btn btn-outline-secondary" onclick="updateQty(${item.id}, ${item.quantity - 1})">-</button>
                            <input type="text" class="form-control text-center qty-input" value="${item.quantity}" readonly>
                            <button class="btn btn-outline-secondary" onclick="updateQty(${item.id}, ${item.quantity + 1})">+</button>
                        </div>
                    </td>
                    <td class="fw-bold">¥${subtotal.toFixed(2)}</td>
                    <td>
                        <button onclick="confirmDelete(${item.id})" class="btn btn-sm btn-outline-danger">删除</button>
                    </td>
                </tr>
            `;
    });

    tbody.innerHTML = html;
    document.getElementById('total-price').innerText = '¥' + total.toFixed(2);
}

// 核心修改：更新数量函数
function updateQty(cartItemId, newQty) {
    if (newQty < 1) {
        // 如果减到0，询问是否删除
        confirmDelete(cartItemId);
        return;
    }

    fetch(`/api/cart/${cartItemId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ quantity: newQty })
    })
        .then(res => {
            if(res.ok) {
                loadCart(); // 刷新列表
            } else {
                showToast('更新失败', 'error');
            }
        });
}

// 触发删除确认
function confirmDelete(id) {
    deleteItemId = id;
    deleteModal.show();
}

// 执行删除
function executeDelete() {
    if (!deleteItemId) return;
    fetch(`/api/cart/${deleteItemId}`, { method: 'DELETE' })
        .then(res => {
            deleteModal.hide();
            if(res.ok) {
                showToast('移除成功');
                loadCart();
            } else {
                showToast('移除失败', 'error');
            }
        });
}

function showCheckoutModal() {
    checkoutModal.show();
}

function submitOrder() {
    const address = document.getElementById('address').value;
    const receiver = document.getElementById('receiverName').value;
    if(!address || !receiver) {
        showToast('请填写完整收货信息', 'error');
        return;
    }

    const orderData = {
        address: address,
        receiverName: receiver,
        items: currentCartItems
    };

    fetch('/api/orders/create', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(orderData)
    })
        .then(res => {
            checkoutModal.hide();
            if(res.ok) {
                showToast('支付成功！邮件已发送');
                setTimeout(() => location.href = '/orders.html', 1500);
            } else {
                showToast('订单提交失败', 'error');
            }
        })
        .catch(() => showToast('网络错误，请重试', 'error'));
}