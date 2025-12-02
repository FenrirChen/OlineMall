function showToast(msg, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    document.getElementById('toast-msg').innerText = msg;
    toastEl.className = `toast align-items-center text-white border-0 bg-${type === 'error' ? 'danger' : 'success'}`;
    new bootstrap.Toast(toastEl).show();
}

function formDataToJson(form) {
    return Object.fromEntries(new FormData(form).entries());
}

document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(formDataToJson(this))
    })
        .then(async res => {
            const data = await res.json();
            if (res.ok) {
                localStorage.setItem('user', JSON.stringify(data.user));
                showToast('登录成功，正在跳转...');
                setTimeout(() => {
                    location.href = data.user.role === 'ADMIN' ? '/admin/dashboard.html' : '/index.html';
                }, 1000);
            } else {
                showToast(data.message || '登录失败', 'error');
            }
        })
        .catch(() => showToast('服务器连接失败', 'error'));
});

document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();
    fetch('/api/auth/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(formDataToJson(this))
    })
        .then(async res => {
            if (res.ok) {
                showToast('注册成功，请登录');
                document.getElementById('login-tab').click();
            } else {
                const data = await res.json();
                showToast(data.message || '注册失败', 'error');
            }
        });
});