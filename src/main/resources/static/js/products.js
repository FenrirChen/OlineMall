let productModal;
let deleteModal;
let currentDeleteId = null;

document.addEventListener('DOMContentLoaded', () => {
    productModal = new bootstrap.Modal(document.getElementById('productModal'));
    deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    loadProducts();
});

function showToast(msg, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    document.getElementById('toast-msg').innerText = msg;
    toastEl.className = `toast align-items-center text-white border-0 bg-${type === 'error' ? 'danger' : 'success'}`;
    new bootstrap.Toast(toastEl).show();
}

function loadProducts() {
    fetch('/api/products')
        .then(res => res.json())
        .then(data => renderTable(data))
        .catch(err => showToast('加载失败', 'error'));
}

function renderTable(data) {
    const tbody = document.getElementById('product-list');
    let html = '';
    data.forEach(p => {
        html += `
                <tr>
                    <td>${p.id}</td>
                    <td><img src="${p.imageUrl || 'https://via.placeholder.com/50'}" class="product-img"></td>
                    <td>${p.name}</td>
                    <td>¥${p.price}</td>
                    <td>
                        <span class="badge ${p.stock > 0 ? 'bg-success' : 'bg-danger'}">
                            ${p.stock > 0 ? p.stock : '缺货'}
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-1" onclick='openModal(${JSON.stringify(p)})'>编辑</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="confirmDelete(${p.id}, '${p.name}')">删除</button>
                    </td>
                </tr>
            `;
    });
    tbody.innerHTML = html;
}

function openModal(product = null) {
    const title = document.getElementById('modalTitle');
    const form = document.getElementById('productForm');

    if (product) {
        title.innerText = '编辑商品';
        document.getElementById('productId').value = product.id;
        document.getElementById('productName').value = product.name;
        document.getElementById('productPrice').value = product.price;
        document.getElementById('productStock').value = product.stock;
        document.getElementById('productImage').value = product.imageUrl;
        document.getElementById('productDesc').value = product.description;
    } else {
        title.innerText = '添加新商品';
        form.reset();
        document.getElementById('productId').value = '';
    }
    productModal.show();
}

function saveProduct() {
    const id = document.getElementById('productId').value;
    const formData = new FormData(document.getElementById('productForm'));
    const data = Object.fromEntries(formData.entries());

    const method = id ? 'PUT' : 'POST';
    // 实际上之前的 Controller 是 PostMapping 统一处理，这里为了兼容 Java 代码：
    // Java 代码中 save 是 @PostMapping，如果 id 为空则 insert，不为空则 update
    // 所以这里统一用 POST 即可，或者前端区分。根据之前代码，Controller 是 @PostMapping

    fetch('/api/products', {
        method: 'POST', // 注意：后端 ProductController.save 是 PostMapping
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    })
        .then(res => {
            if(res.ok) {
                showToast('保存成功');
                productModal.hide();
                loadProducts();
            } else {
                showToast('保存失败', 'error');
            }
        });
}

function confirmDelete(id, name) {
    currentDeleteId = id;
    document.getElementById('del-product-name').innerText = name;
    deleteModal.show();
}

function executeDelete() {
    if (!currentDeleteId) return;
    fetch(`/api/products/${currentDeleteId}`, { method: 'DELETE' })
        .then(res => {
            deleteModal.hide();
            if(res.ok) {
                showToast('删除成功');
                loadProducts();
            } else {
                showToast('删除失败', 'error');
            }
        });
}