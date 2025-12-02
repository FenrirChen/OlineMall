document.addEventListener('DOMContentLoaded', function() {
    loadUsers();
    loadLogs();
});

// 1. 加载真实用户列表
function loadUsers() {
    fetch('/api/admin/users')
        .then(res => res.json())
        .then(users => {
            const tbody = document.getElementById('user-list');
            let html = '';
            users.forEach(u => {
                html += `
                        <tr>
                            <td>${u.id}</td>
                            <td>${u.username}</td>
                            <td>${u.email || '-'}</td>
                            <td><span class="badge ${u.role === 'ADMIN' ? 'bg-danger' : 'bg-success'}">${u.role}</span></td>
                            <td>${new Date(u.createTime).toLocaleString()}</td>
                        </tr>
                    `;
            });
            tbody.innerHTML = html;
        });
}

// 2. 加载真实日志
function loadLogs() {
    const filterType = document.getElementById('log-filter').value;
    let url = '/api/admin/logs';
    if (filterType) url += `?type=${filterType}`;

    fetch(url)
        .then(res => res.json())
        .then(logs => {
            const tbody = document.getElementById('log-list');
            if (logs.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">暂无日志</td></tr>';
                return;
            }
            let html = '';
            logs.forEach(log => {
                let typeClass = '';
                if(log.actionType === 'ORDER') typeClass = 'log-type-order';
                else if(log.actionType === 'LOGIN') typeClass = 'log-type-login';
                else typeClass = 'log-type-view';

                html += `
                        <tr>
                            <td class="text-muted small">${new Date(log.createTime).toLocaleString()}</td>
                            <td>${log.userUsername}</td>
                            <td class="${typeClass}">${log.actionType}</td>
                            <td>${log.details}</td>
                        </tr>
                    `;
            });
            tbody.innerHTML = html;
        });
}