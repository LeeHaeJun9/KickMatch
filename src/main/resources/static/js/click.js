document.addEventListener('DOMContentLoaded', () => {
    console.log('DOMContentLoaded fired'); // ✅ 확인용 로그

    const rows = document.querySelectorAll('table tbody tr');
    console.log(rows.length + ' rows found'); // ✅ 몇 개의 row가 잡혔는지 확인

    rows.forEach(row => {
        row.addEventListener('click', () => {
            const link = row.dataset.href;
            console.log('Navigating to: ' + link); // ✅ 클릭 시 이동 URL 확인
            if (link) {
                window.location.href = link;
            }
        });
    });
});
