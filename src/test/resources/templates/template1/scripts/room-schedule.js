function getDataUrl() {
    const idx = document.URL.indexOf('?');
    if (idx !== -1) {
        return document.URL.substring(idx + 1, document.URL.length);
    }
    return null;
}

function esc(s) {
    const d = document.createElement('div');
    d.textContent = s ?? '';
    return d.innerHTML;
}

function render(data) {
    document.getElementById('roomName').textContent = data.room.type;
    document.getElementById('roomNameEn').textContent = data.room.typeEn;
    document.getElementById('displayDate').textContent = data.date;
    document.getElementById('roomId').textContent = data.room.name;
    document.getElementById('lastChanged').textContent = data.room.lastChanged;

    if (data.room.scheduleUrl) {
        generateQR(data.room.scheduleUrl);
    }

    const list = document.getElementById('scheduleList');
    list.innerHTML = '';
    data.slots.forEach(s => {
        const el = document.createElement('div');
        el.className = 'slot ' + (s.type || '');
        el.innerHTML =
            '<div class="time-col">' +
            '<span class="time-start">' + esc(s.timeStart) + '</span>' +
            '<div class="time-divider"></div>' +
            '<span class="time-end">' + esc(s.timeEnd) + '</span>' +
            '</div>' +
            '<div class="content-col">' +
            '<div class="slot-title">' + esc(s.title) +
            (s.movedTo ? '<span class="slot-moved"><span class="slot-moved-arrow">→</span>' + esc(s.movedTo) + '</span>' : '') +
            '</div>' +
            (s.titleEn ? '<div class="slot-subtitle">' + esc(s.titleEn) + '</div>' : '') +
            '</div>';
        list.appendChild(el);
    });

    console.log("template rendered");
}

function generateQR(url) {
    const el = document.getElementById('qrcode');
    el.innerHTML = '';
    new QRCode(el, {
        text: url,
        width: 200,
        height: 200,
        colorDark: '#000000',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.M
    });
}

function scaleCanvas() {
    const scale = Math.min(window.innerWidth / 1200, window.innerHeight / 1600);
    const offsetX = (window.innerWidth - 1200 * scale) / 2;
    document.body.style.transform = `scale(${scale})`;
    document.body.style.marginLeft = offsetX + 'px';
}

window.addEventListener('resize', scaleCanvas);
scaleCanvas();

// MOCK - ersetzen mit: fetch('/api/room/F01-109').then(r => r.json()).then(render);
fetch(getDataUrl()).then(r => r.json()).then(render);
