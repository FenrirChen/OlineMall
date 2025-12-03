// 页面加载入口
document.addEventListener('DOMContentLoaded', function() {
    checkUserRole(); // 1. 先检查角色，如果是管理员直接踢走
    // 2. 如果是顾客或游客，继续加载页面
    loadProducts();
});

function checkUserRole() {
    const userStr = localStorage.getItem('user');

    if (userStr) {
        const user = JSON.parse(userStr);

        // 如果是管理员访问首页，自动跳转到后台仪表盘
        if (user.role === 'ADMIN') {
            window.location.replace('/admin/dashboard.html');
            return;
        }

        // 如果是普通顾客，更新导航栏
        document.getElementById('welcome-msg').textContent = `欢迎, ${user.username}`;
        document.getElementById('login-nav-item').classList.add('d-none');
        document.getElementById('cart-nav-item').classList.remove('d-none');
        document.getElementById('order-nav-item').classList.remove('d-none');
        document.getElementById('logout-nav-item').classList.remove('d-none');
    } else {
        // 游客状态
        document.getElementById('welcome-msg').textContent = '欢迎, 游客';
    }

    // 检查完毕，显示页面
    document.body.style.visibility = 'visible';
}

function showToast(msg, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    document.getElementById('toast-msg').innerText = msg;
    toastEl.className = `toast align-items-center text-white border-0 bg-${type === 'error' ? 'danger' : 'success'}`;
    new bootstrap.Toast(toastEl).show();
}

function loadProducts(keyword = '') {
    let url = '/api/products';
    if (keyword) url += `?name=${encodeURIComponent(keyword)}`;

    fetch(url)
        .then(response => response.json())
        .then(data => renderProducts(data))
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('product-list').innerHTML = '<p class="text-center text-danger">加载失败</p>';
        });
}

function renderProducts(products) {
    const container = document.getElementById('product-list');
    if (products.length === 0) {
        container.innerHTML = '<div class="col-12 text-center text-muted">没有找到相关商品</div>';
        return;
    }

    let html = '';
    products.forEach(p => {
        const imgUrl = p.imageUrl || 'https://via.placeholder.com/300x200?text=Product';
        html += `
                <div class="col-md-3 mb-4">
                    <div class="card product-card h-100">
                        <img src="${imgUrl}" class="card-img-top" alt="${p.name}">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">${p.name}</h5>
                            <p class="card-text text-truncate">${p.description || '暂无描述'}</p>
                            <div class="mt-auto">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="text-danger fw-bold fs-5">¥${p.price}</span>
                                    <small class="text-muted">库存: ${p.stock}</small>
                                </div>
                                <div class="input-group mb-2">
                                    <span class="input-group-text">数量</span>
                                    <input type="number" id="qty-${p.id}" class="form-control text-center" value="1" min="1" max="${p.stock}">
                                </div>
                                <button onclick="addToCart(${p.id})" class="btn btn-primary w-100" ${p.stock <= 0 ? 'disabled' : ''}>
                                    ${p.stock <= 0 ? '缺货' : '加入购物车'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
    });
    container.innerHTML = html;
}

function searchProducts() {
    const keyword = document.getElementById('searchInput').value;
    loadProducts(keyword);
}

function addToCart(productId) {
    if (!localStorage.getItem('user')) {
        showToast('请先登录', 'error');
        setTimeout(() => location.href = '/login.html', 1000);
        return;
    }

    const qtyInput = document.getElementById(`qty-${productId}`);
    const quantity = qtyInput ? parseInt(qtyInput.value) : 1;

    if (quantity < 1) {
        showToast('数量至少为 1', 'error');
        return;
    }

    fetch('/api/cart/add', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ productId: productId, quantity: quantity })
    })
        .then(res => {
            if(res.ok) {
                showToast(`已添加 ${quantity} 件商品到购物车！`);
            } else if (res.status === 401) {
                showToast('登录过期，请重新登录', 'error');
                setTimeout(() => location.href = '/login.html', 1500);
            } else {
                showToast('添加失败', 'error');
            }
        });
}

function logout() {
    localStorage.removeItem('user');
    fetch('/api/auth/logout', { method: 'POST' }).finally(() => {
        location.href = '/login.html';
    });
}