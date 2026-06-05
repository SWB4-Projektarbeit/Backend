const DATA = {
    room: {
        id: "F 01.-109",
        name: "Vorlesungssaal",
        nameEn: "Lecture Hall",
        lastChanged: "15.30",
        scheduleUrl: "https://www3.hs-esslingen.de/qislsf/rds?state=wplan&act=Raum&pool=Raum&raum.rgid=318"
    },
    date: "15.04.2026",
    slots: [
        {
            timeStart: "09:45",
            timeEnd: "13:00",
            title: "Physik fuer Ingenieure",
            titleEn: "Physics for Engineers",
            type: "booked"
        },
        {timeStart: "09:45", timeEnd: "13:00", title: "FREI - 30 Min", titleEn: "FREE - 30 Min", type: "active"},
        {
            timeStart: "09:45",
            timeEnd: "13:00",
            title: "Softwareentwicklung → F 01.-211",
            titleEn: "Software Development",
            type: "booked"
        },
        {timeStart: "09:45", timeEnd: "13:00", title: "FREI - 30 Min", titleEn: "FREE - 30 Min", type: "free"},
        {timeStart: "09:45", timeEnd: "13:00", title: "Datenbanken", titleEn: "Databases", type: "cancelled"},
        {timeStart: "09:45", timeEnd: "13:00", title: "FREI - 30 Min", titleEn: "FREE - 30 Min", type: "free"},
    ]
};

function esc(s) {
    const d = document.createElement('div');
    d.textContent = s ?? '';
    return d.innerHTML;
}

function render(data) {
    document.getElementById('roomName').textContent = data.room.name;
    document.getElementById('roomNameEn').textContent = data.room.nameEn;
    document.getElementById('displayDate').textContent = data.date;
    document.getElementById('roomId').textContent = data.room.id;
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
            '<div class="slot-title">' + esc(s.title) + '</div>' +
            (s.titleEn ? '<div class="slot-subtitle">' + esc(s.titleEn) + '</div>' : '') +
            '</div>';
        list.appendChild(el);
    });
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

// MOCK - ersetzen mit: fetch('/api/room/F01-109').then(r => r.json()).then(render);
render(DATA);
