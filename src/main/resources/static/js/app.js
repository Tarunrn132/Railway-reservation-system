// ===================================================================
// RailReserve - Real-Time Railway Reservation Application JS
// ===================================================================

const API_BASE = '/api';

// Application State
const state = {
  currentUser: JSON.parse(localStorage.getItem('railway_user')) || null,
  trains: [],
  selectedTrain: null,
  pnrPendingCancellation: null,
  activeFilter: 'ALL',
  userBookings: [],
  currentUnitFare: 0,
  passengers: [
    { name: '', age: '', gender: 'MALE', berthPreference: 'NONE' }
  ],
  pendingBookingPayload: null
};

// ===================================================================
// Initialization
// ===================================================================
document.addEventListener('DOMContentLoaded', () => {
  initDateInputs();
  updateAuthUI();
  initStationAutocomplete();
  loadInitialTrains();
});

function initDateInputs() {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, '0');
  const dd = String(today.getDate()).padStart(2, '0');
  const minDate = `${yyyy}-${mm}-${dd}`;

  // Default search date = tomorrow
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tYyyy = tomorrow.getFullYear();
  const tMm = String(tomorrow.getMonth() + 1).padStart(2, '0');
  const tDd = String(tomorrow.getDate()).padStart(2, '0');
  const defaultDate = `${tYyyy}-${tMm}-${tDd}`;

  const searchDateInput = document.getElementById('search-date');
  if (searchDateInput) {
    searchDateInput.min = minDate;
    searchDateInput.value = defaultDate;
  }

  const bookDateInput = document.getElementById('book-date');
  if (bookDateInput) {
    bookDateInput.min = minDate;
  }
}

// ===================================================================
// Navigation & Tab Management
// ===================================================================
function showSection(sectionName) {
  const sections = ['home', 'bookings', 'pnr', 'profile'];
  sections.forEach(s => {
    const el = document.getElementById(`section-${s}`);
    const navEl = document.getElementById(`nav-${s}`);
    if (el) el.style.display = s === sectionName ? 'block' : 'none';
    if (navEl) {
      if (s === sectionName) navEl.classList.add('active');
      else navEl.classList.remove('active');
    }
  });

  const heroBanner = document.getElementById('hero-banner');
  const features = document.getElementById('features-section');

  if (sectionName === 'home') {
    if (heroBanner) heroBanner.style.display = 'block';
    if (features) features.style.display = 'grid';
  } else {
    if (heroBanner) heroBanner.style.display = 'none';
    if (features) features.style.display = 'none';
  }

  if (sectionName === 'bookings') {
    loadUserBookings('ALL');
  }

  if (sectionName === 'profile') {
    loadUserProfile();
  }
}

// ===================================================================
// Stations Autocomplete & Initial Trains
// ===================================================================
function initStationAutocomplete() {
  setupAutocompleteInput('search-from', 'search-from-code', 'from-autocomplete-dropdown');
  setupAutocompleteInput('search-to', 'search-to-code', 'to-autocomplete-dropdown');

  // Pre-fill default popular pair (Bengaluru to Chennai)
  const fromEl = document.getElementById('search-from');
  const fromCode = document.getElementById('search-from-code');
  const toEl = document.getElementById('search-to');
  const toCode = document.getElementById('search-to-code');

  if (fromEl && !fromEl.value) {
    fromEl.value = 'Bengaluru City Junction (SBC)';
    if (fromCode) fromCode.value = 'SBC';
  }
  if (toEl && !toEl.value) {
    toEl.value = 'Chennai Central (MAS)';
    if (toCode) toCode.value = 'MAS';
  }

  // Close dropdowns on outside click
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.autocomplete-container')) {
      document.querySelectorAll('.autocomplete-dropdown').forEach(dd => dd.classList.remove('active'));
    }
  });
}

function setupAutocompleteInput(inputId, codeHiddenId, dropdownId) {
  const input = document.getElementById(inputId);
  const codeHidden = document.getElementById(codeHiddenId);
  const dropdown = document.getElementById(dropdownId);
  if (!input || !dropdown) return;

  let debounceTimer = null;

  input.addEventListener('input', () => {
    const q = input.value.trim();
    clearTimeout(debounceTimer);
    if (!q) {
      if (codeHidden) codeHidden.value = '';
      dropdown.classList.remove('active');
      return;
    }

    debounceTimer = setTimeout(async () => {
      try {
        const res = await fetch(`${API_BASE}/stations/search?q=${encodeURIComponent(q)}`);
        const data = await res.json();
        if (data.success && data.data && data.data.length > 0) {
          dropdown.innerHTML = data.data.slice(0, 10).map(st => `
            <div class="autocomplete-item" onclick="selectStation('${inputId}', '${codeHiddenId}', '${dropdownId}', '${escapeHtml(st.stationName)}', '${escapeHtml(st.stationCode)}', '${escapeHtml(st.city)}', '${escapeHtml(st.state)}')">
              <div>
                <div class="autocomplete-item-name">${escapeHtml(st.stationName)}</div>
                <div class="autocomplete-item-sub">${escapeHtml(st.city)}, ${escapeHtml(st.state)}</div>
              </div>
              <span class="autocomplete-item-code">${escapeHtml(st.stationCode)}</span>
            </div>
          `).join('');
          dropdown.classList.add('active');
        } else {
          dropdown.innerHTML = '<div style="padding: 10px; font-size: 0.85rem; color: #64748b;">No matching stations found</div>';
          dropdown.classList.add('active');
        }
      } catch (err) {
        console.error('Station search error:', err);
      }
    }, 200);
  });

  input.addEventListener('focus', () => {
    if (input.value.trim()) {
      input.dispatchEvent(new Event('input'));
    }
  });
}

function selectStation(inputId, codeHiddenId, dropdownId, name, code, city, state) {
  const input = document.getElementById(inputId);
  const codeHidden = document.getElementById(codeHiddenId);
  const dropdown = document.getElementById(dropdownId);

  if (input) input.value = `${name} (${code})`;
  if (codeHidden) codeHidden.value = code;
  if (dropdown) dropdown.classList.remove('active');
}

async function loadInitialTrains() {
  const searchDate = document.getElementById('search-date')?.value || '';
  try {
    const res = await fetch(`${API_BASE}/trains?date=${encodeURIComponent(searchDate)}`);
    const data = await res.json();
    if (data.success) {
      state.trains = data.data;
      renderTrainList(data.data, 'Authentic Indian Railways Master Catalogue');
    }
  } catch (err) {
    console.error('Failed to load initial trains:', err);
    showToast('Failed to connect to backend server', 'error');
  }
}

function swapStations() {
  const fromEl = document.getElementById('search-from');
  const fromCode = document.getElementById('search-from-code');
  const toEl = document.getElementById('search-to');
  const toCode = document.getElementById('search-to-code');

  if (fromEl && toEl) {
    const tempVal = fromEl.value;
    fromEl.value = toEl.value;
    toEl.value = tempVal;

    if (fromCode && toCode) {
      const tempCode = fromCode.value;
      fromCode.value = toCode.value;
      toCode.value = tempCode;
    }
  }
}

// ===================================================================
// Train Search & Rendering
// ===================================================================
async function handleSearchTrains(e) {
  e.preventDefault();
  const fromInput = document.getElementById('search-from').value.trim();
  const toInput = document.getElementById('search-to').value.trim();
  const fromCode = document.getElementById('search-from-code')?.value.trim();
  const toCode = document.getElementById('search-to-code')?.value.trim();
  const date = document.getElementById('search-date').value;

  const source = fromCode || fromInput;
  const destination = toCode || toInput;

  if (source.toLowerCase() === destination.toLowerCase()) {
    showToast('Source and Destination cannot be the same station', 'error');
    return;
  }

  const submitBtn = document.getElementById('search-submit-btn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Searching...';

  try {
    const url = `${API_BASE}/trains/search?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}&date=${encodeURIComponent(date)}`;
    const res = await fetch(url);
    const data = await res.json();

    if (data.success) {
      state.trains = data.data;
      renderTrainList(data.data, `${fromInput} → ${toInput} (${formatDateDisplay(date)})`);
      if (data.data.length === 0) {
        showToast(`No direct trains found between ${fromInput} and ${toInput}`, 'info');
      } else {
        showToast(`Found ${data.data.length} authentic train(s)`, 'success');
      }
    } else {
      showToast(data.message || 'Search failed', 'error');
    }
  } catch (err) {
    console.error('Train search error:', err);
    showToast('Failed to connect to server', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = '🔍 Search Trains';
  }
}

function renderTrainList(trains, contextLabel) {
  const container = document.getElementById('trains-container');
  const contextEl = document.getElementById('search-context-label');
  const countEl = document.getElementById('results-count');

  if (contextEl) contextEl.textContent = contextLabel ? `• ${contextLabel}` : '';
  if (countEl) countEl.textContent = `${trains.length} Trains available`;

  if (!trains || trains.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🚆</div>
        <h3>No Trains Found</h3>
        <p>Try searching for major Indian station pairs (e.g. SBC to MAS, NDLS to MMCT, or MMCT to ADI).</p>
        <button class="btn btn-outline btn-sm" onclick="loadInitialTrains()">Show Complete Catalogue</button>
      </div>
    `;
    return;
  }

  container.innerHTML = trains.map(train => {
    let seatBadgeClass = 'seats-available';
    let seatText = `${train.availableSeats} Seats Available`;

    if (train.availableSeats <= 0) {
      seatBadgeClass = 'seats-full';
      seatText = 'Seats Full';
    } else if (train.availableSeats < 15) {
      seatBadgeClass = 'seats-low';
      seatText = `Only ${train.availableSeats} Left`;
    }

    return `
      <div class="train-card">
        <div class="train-main-info">
          <div style="display: flex; gap: 0.5rem; align-items: center;">
            <span class="train-badge"># ${escapeHtml(train.trainNumber)}</span>
            <span style="font-size: 0.75rem; background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-weight: 600; color: #475569;">${escapeHtml(train.trainType || 'Express')}</span>
          </div>
          <h3 class="train-name">${escapeHtml(train.trainName)}</h3>
          <div class="train-route">
            <strong>${escapeHtml(train.source)} (${escapeHtml(train.sourceCode || '')})</strong> ➔ 
            <strong>${escapeHtml(train.destination)} (${escapeHtml(train.destinationCode || '')})</strong>
          </div>
          <div style="font-size: 0.8rem; color: #64748b; margin-top: 4px;">
            Classes: ${escapeHtml(train.trainClass || 'SL, 3A, 2A, 1A')} | Runs: ${escapeHtml(train.runningDays || 'Daily')}
          </div>
        </div>

        <div class="train-timings">
          <div class="time-box">
            <h4>${escapeHtml(train.departureTime)}</h4>
            <span>${escapeHtml(train.source)} (${escapeHtml(train.sourceCode || '')})</span>
          </div>

          <div class="duration-line">
            <span>Direct</span>
            <div class="duration-bar"></div>
            <span>${train.distanceKm ? train.distanceKm + ' km' : 'Daily'}</span>
          </div>

          <div class="time-box">
            <h4>${escapeHtml(train.arrivalTime)}</h4>
            <span>${escapeHtml(train.destination)} (${escapeHtml(train.destinationCode || '')})</span>
          </div>
        </div>

        <div class="train-booking-action">
          <div class="fare-tag">₹${train.fare} <small>base fare</small></div>
          <span class="seats-badge ${seatBadgeClass}">${seatText}</span>
          <div style="display: flex; flex-direction: column; gap: 0.4rem; margin-top: 6px; width: 100%;">
            <button class="btn btn-primary btn-sm" onclick="openBookingModal(${train.id})" ${train.availableSeats <= 0 ? 'disabled' : ''}>
              ${train.availableSeats <= 0 ? 'Full' : 'Book Ticket ➔'}
            </button>
            <button class="btn btn-outline btn-sm" onclick="viewTrainScheduleModal('${escapeHtml(train.trainNumber)}')" style="font-size: 0.75rem; padding: 4px 8px;">
              🗺️ View Route & Stops
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

async function viewTrainScheduleModal(trainNumber) {
  try {
    showToast('Fetching train route timetable...', 'info');
    const res = await fetch(`${API_BASE}/trains/${encodeURIComponent(trainNumber)}/schedule`);
    const data = await res.json();
    if (data.success) {
      const schedule = data.data;
      document.getElementById('train-details-title').textContent = `${schedule.trainName} (#${schedule.trainNumber})`;
      document.getElementById('train-details-subtitle').textContent = `${schedule.source} (${schedule.sourceCode || ''}) ➔ ${schedule.destination} (${schedule.destinationCode || ''})`;
      document.getElementById('td-type').textContent = schedule.trainType || 'Express';
      document.getElementById('td-running-days').textContent = schedule.runningDays || 'Daily';
      document.getElementById('td-distance').textContent = schedule.distanceKm ? `${schedule.distanceKm} km` : '-- km';
      document.getElementById('td-classes').textContent = 'SL, 3A, 2A, 1A, CC, EC';

      const tbody = document.getElementById('train-schedule-tbody');
      if (schedule.routeStops && schedule.routeStops.length > 0) {
        tbody.innerHTML = schedule.routeStops.map(stop => `
          <tr style="border-bottom: 1px solid #e2e8f0;">
            <td style="padding: 8px 12px; font-weight: 600; color: #64748b;">${stop.stopNumber}</td>
            <td style="padding: 8px 12px;"><strong>${escapeHtml(stop.stationName)}</strong> <span class="badge" style="background:#eff6ff;color:#1e40af;font-size:0.75rem;padding:2px 6px;border-radius:4px;border:1px solid #bfdbfe;">${escapeHtml(stop.stationCode)}</span></td>
            <td style="padding: 8px 12px; color: #475569;">${escapeHtml(stop.city || '')}${stop.state ? ', ' + escapeHtml(stop.state) : ''}</td>
            <td style="padding: 8px 12px; font-weight: 600; color: #047857;">${escapeHtml(stop.arrivalTime || '--')}</td>
            <td style="padding: 8px 12px; font-weight: 600; color: #1e40af;">${escapeHtml(stop.departureTime || '--')}</td>
            <td style="padding: 8px 12px; color: #64748b;">${stop.distanceKm || 0} km</td>
          </tr>
        `).join('');
      } else {
        tbody.innerHTML = `<tr><td colspan="6" style="padding: 12px; text-align: center; color: #64748b;">No intermediate stops recorded for this train.</td></tr>`;
      }

      const bookBtn = document.getElementById('td-book-btn');
      bookBtn.onclick = () => {
        closeModal('trainDetailsModal');
        const found = state.trains.find(t => t.trainNumber === trainNumber);
        if (found) {
          openBookingModal(found.id);
        }
      };

      openModal('trainDetailsModal');
    } else {
      showToast(data.message || 'Failed to load timetable', 'error');
    }
  } catch (err) {
    console.error('Error fetching schedule:', err);
    showToast('Failed to load train route', 'error');
  }
}

// ===================================================================
// Multi-Passenger Management & Booking Flow
// ===================================================================
function openBookingModal(trainId) {
  const train = state.trains.find(t => t.id === trainId);
  if (!train) return;

  state.selectedTrain = train;
  document.getElementById('book-train-id').value = train.id;
  document.getElementById('book-train-name').textContent = `${train.trainName} (#${train.trainNumber})`;
  document.getElementById('book-train-route').textContent = `${train.source} (${train.sourceCode || ''}) → ${train.destination} (${train.destinationCode || ''}) | Dep: ${train.departureTime} | Arr: ${train.arrivalTime}`;

  const searchDate = document.getElementById('search-date')?.value || '';
  const dateInput = document.getElementById('book-date');
  if (dateInput) {
    dateInput.value = train.searchDate || searchDate;
  }

  const classSelect = document.getElementById('book-class');
  classSelect.innerHTML = (train.availableClasses || ['Sleeper (SL)', 'AC 3 Tier (3A)', 'AC 2 Tier (2A)', 'AC First Class (1A)'])
    .map(cls => `<option value="${cls}">${cls}</option>`)
    .join('');

  // Reset passengers with current user as first passenger if available
  state.passengers = [
    {
      name: state.currentUser ? state.currentUser.name : '',
      age: 28,
      gender: 'MALE',
      berthPreference: 'NONE'
    }
  ];

  renderPassengerRows();
  onBookingDateOrClassChange();
  openModal('bookingModal');
}

function renderPassengerRows() {
  const container = document.getElementById('passengers-list-container');
  if (!container) return;

  container.innerHTML = state.passengers.map((p, idx) => `
    <div class="passenger-row" style="background: #f8fafc; border: 1px solid var(--border-color); padding: 0.75rem; border-radius: 6px; margin-bottom: 0.5rem;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
        <span style="font-weight: 600; font-size: 0.85rem; color: var(--primary);">Passenger #${idx + 1}</span>
        ${state.passengers.length > 1 ? `
          <button type="button" class="btn btn-outline btn-sm" onclick="removePassengerRow(${idx})" style="padding: 1px 6px; color: var(--danger); font-size: 0.75rem;">✕ Remove</button>
        ` : ''}
      </div>
      <div style="display: grid; grid-template-columns: 2fr 1fr 1.2fr 1.5fr; gap: 0.5rem;">
        <div>
          <input type="text" class="form-control form-control-sm" placeholder="Full Name *" value="${escapeHtml(p.name)}" required oninput="updatePassengerData(${idx}, 'name', this.value)">
        </div>
        <div>
          <input type="number" class="form-control form-control-sm" placeholder="Age *" min="1" max="120" value="${p.age || ''}" required oninput="updatePassengerData(${idx}, 'age', this.value)">
        </div>
        <div>
          <select class="form-control form-control-sm" onchange="updatePassengerData(${idx}, 'gender', this.value)">
            <option value="MALE" ${p.gender === 'MALE' ? 'selected' : ''}>Male</option>
            <option value="FEMALE" ${p.gender === 'FEMALE' ? 'selected' : ''}>Female</option>
            <option value="OTHER" ${p.gender === 'OTHER' ? 'selected' : ''}>Other</option>
          </select>
        </div>
        <div>
          <select class="form-control form-control-sm" onchange="updatePassengerData(${idx}, 'berthPreference', this.value)">
            <option value="NONE" ${p.berthPreference === 'NONE' ? 'selected' : ''}>No Berth Pref</option>
            <option value="LOWER" ${p.berthPreference === 'LOWER' ? 'selected' : ''}>Lower Berth</option>
            <option value="MIDDLE" ${p.berthPreference === 'MIDDLE' ? 'selected' : ''}>Middle Berth</option>
            <option value="UPPER" ${p.berthPreference === 'UPPER' ? 'selected' : ''}>Upper Berth</option>
            <option value="SIDE_LOWER" ${p.berthPreference === 'SIDE_LOWER' ? 'selected' : ''}>Side Lower</option>
          </select>
        </div>
      </div>
    </div>
  `).join('');

  updateBookingPriceBreakdown();
}

function addPassengerRow() {
  if (state.passengers.length >= 6) {
    showToast('Maximum 6 passengers allowed per booking', 'info');
    return;
  }
  state.passengers.push({ name: '', age: '', gender: 'MALE', berthPreference: 'NONE' });
  renderPassengerRows();
}

function removePassengerRow(index) {
  if (state.passengers.length <= 1) return;
  state.passengers.splice(index, 1);
  renderPassengerRows();
}

function updatePassengerData(index, field, value) {
  if (state.passengers[index]) {
    state.passengers[index][field] = value;
  }
  if (field === 'name' || field === 'age') {
    // No full re-render to avoid losing focus
  }
}

async function onBookingDateOrClassChange() {
  if (!state.selectedTrain) return;
  const trainNumber = state.selectedTrain.trainNumber;
  const journeyDate = document.getElementById('book-date').value;
  const travelClass = document.getElementById('book-class').value;
  const quota = document.getElementById('book-quota')?.value || 'GN';
  const badgeContainer = document.getElementById('booking-availability-badge');

  badgeContainer.innerHTML = '<div style="font-size: 0.8rem; color: #64748b;">Checking seat availability and fare breakdown...</div>';

  try {
    const availUrl = `${API_BASE}/trains/${encodeURIComponent(trainNumber)}/availability?date=${encodeURIComponent(journeyDate)}&travelClass=${encodeURIComponent(travelClass)}&quota=${encodeURIComponent(quota)}`;
    const availRes = await fetch(availUrl);
    const availData = await availRes.json();

    if (availData.success && availData.data) {
      const avail = availData.data;
      const matchedClass = (avail.classAvailabilities || []).find(c => c.travelClass === travelClass || travelClass.includes(c.travelClass)) || (avail.classAvailabilities && avail.classAvailabilities[0]);

      let badgeHtml = '';
      if (matchedClass && matchedClass.status === 'AVAILABLE') {
        badgeHtml = `<span class="badge badge-confirmed" style="font-size: 0.85rem; padding: 4px 10px;">🟢 Seats Available: ${matchedClass.seats} (${matchedClass.statusDetails})</span>`;
      } else if (matchedClass) {
        badgeHtml = `<span class="badge badge-cancelled" style="font-size: 0.85rem; padding: 4px 10px;">🟠 Status: ${escapeHtml(matchedClass.statusDetails || matchedClass.status)}</span>`;
      } else {
        badgeHtml = `<span class="badge badge-confirmed" style="font-size: 0.85rem; padding: 4px 10px;">🟢 Seats Available</span>`;
      }

      badgeContainer.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 6px 12px; border-radius: 6px; border: 1px solid var(--border-color);">
          <div>${badgeHtml}</div>
          <div style="font-size: 0.75rem; color: #64748b;">Source: ${escapeHtml(avail.dataSource || 'Indian Railways Master Timetable')}</div>
        </div>
      `;
    }
  } catch (e) {
    badgeContainer.innerHTML = '';
  }

  // Fetch Itemized Fare
  try {
    const fareUrl = `${API_BASE}/trains/${encodeURIComponent(trainNumber)}/fare?date=${encodeURIComponent(journeyDate)}`;
    const fareRes = await fetch(fareUrl);
    const fareData = await fareRes.json();

    if (fareData.success && fareData.data && fareData.data.classFares) {
      const matchedFare = fareData.data.classFares.find(f => f.travelClass === travelClass || travelClass.includes(f.travelClass));
      if (matchedFare) {
        state.currentUnitFare = matchedFare.totalFare;
        renderFareBreakdown(matchedFare);
        return;
      }
    }
  } catch (e) {
    console.error('Fare fetch error:', e);
  }

  updateBookingPriceBreakdown();
}

function renderFareBreakdown(fareItem) {
  const count = state.passengers.length;
  const unitFare = fareItem.totalFare;
  state.currentUnitFare = unitFare;
  const total = Math.round(unitFare * count * 100) / 100;

  const fareDetailEl = document.getElementById('fare-calc-detail');
  const fareTotalEl = document.getElementById('book-total-fare');

  if (fareDetailEl) {
    fareDetailEl.innerHTML = `
      ${count} Passenger(s) × ₹${unitFare} <br>
      <small style="color: #64748b;">Base: ₹${fareItem.baseFare} | Res: ₹${fareItem.reservationCharge} | SF: ₹${fareItem.superfastCharge} | GST: ₹${fareItem.gst}</small>
    `;
  }
  if (fareTotalEl) fareTotalEl.textContent = `₹${total}`;
}

function updateBookingPriceBreakdown() {
  if (!state.selectedTrain) return;
  const baseFare = state.selectedTrain.fare;
  const selectedClass = document.getElementById('book-class').value;
  let multiplier = 1.0;

  const normalized = selectedClass.toUpperCase();
  if (normalized.includes('1A') || normalized.includes('FIRST') || normalized.includes('1 TIER') || normalized.includes('EXECUTIVE') || normalized.includes('EC')) {
    multiplier = 2.8;
  } else if (normalized.includes('2A') || normalized.includes('2 TIER')) {
    multiplier = 2.0;
  } else if (normalized.includes('3A') || normalized.includes('3 TIER')) {
    multiplier = 1.4;
  } else if (normalized.includes('CC') || normalized.includes('CHAIR')) {
    multiplier = 1.2;
  } else if (normalized.includes('2S') || normalized.includes('SECOND')) {
    multiplier = 0.6;
  }

  const unitFare = Math.round(baseFare * multiplier * 100) / 100;
  state.currentUnitFare = unitFare;

  const count = state.passengers.length;
  const total = Math.round(unitFare * count * 100) / 100;

  const fareDetailEl = document.getElementById('fare-calc-detail');
  const fareTotalEl = document.getElementById('book-total-fare');

  if (fareDetailEl) fareDetailEl.textContent = `${count} Passenger(s) × ₹${unitFare}`;
  if (fareTotalEl) fareTotalEl.textContent = `₹${total}`;
}

async function handleBookingSubmit(e) {
  e.preventDefault();

  // Validate passenger list
  for (let i = 0; i < state.passengers.length; i++) {
    const p = state.passengers[i];
    if (!p.name || !p.name.trim()) {
      showToast(`Please enter name for Passenger #${i + 1}`, 'error');
      return;
    }
    if (!p.age || isNaN(p.age) || p.age < 1) {
      showToast(`Please enter valid age for Passenger #${i + 1}`, 'error');
      return;
    }
  }

  const trainId = parseInt(document.getElementById('book-train-id').value, 10);
  const journeyDate = document.getElementById('book-date').value;
  const travelClass = document.getElementById('book-class').value;
  const quota = document.getElementById('book-quota').value;

  const firstPassenger = state.passengers[0];
  const totalFare = Math.round(state.currentUnitFare * state.passengers.length * 100) / 100;

  const payload = {
    userId: state.currentUser ? state.currentUser.id : null,
    trainId,
    passengerName: firstPassenger.name,
    age: parseInt(firstPassenger.age, 10),
    gender: firstPassenger.gender,
    phone: state.currentUser ? state.currentUser.phone : '9876543210',
    journeyDate,
    travelClass,
    quota,
    passengers: state.passengers.map(p => ({
      name: p.name.trim(),
      age: parseInt(p.age, 10),
      gender: p.gender,
      berthPreference: p.berthPreference
    }))
  };

  state.pendingBookingPayload = payload;

  // Open Payment Checkout Modal
  openPaymentCheckout(totalFare);
}

// ===================================================================
// Payment Gateway Integration & Confirmation
// ===================================================================
async function openPaymentCheckout(amount) {
  document.getElementById('pay-modal-amount').textContent = `₹${amount}`;
  document.getElementById('pay-modal-receipt').textContent = `Booking Order #RCP_${Date.now()}`;
  openModal('paymentModal');
}

async function executePaymentVerification() {
  const btn = document.getElementById('pay-confirm-btn');
  btn.disabled = true;
  btn.textContent = 'Processing Payment...';

  try {
    // 1. Create Payment Order
    const totalAmount = Math.round(state.currentUnitFare * state.passengers.length * 100) / 100;
    const orderRes = await fetch(`${API_BASE}/payments/create-order`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        amount: totalAmount,
        currency: 'INR',
        receipt: `RCP_${Date.now()}`
      })
    });
    const orderData = await orderRes.json();
    if (!orderData.success) {
      showToast('Payment gateway initialization failed', 'error');
      btn.disabled = false;
      btn.textContent = 'Authorize & Pay Now';
      return;
    }

    const orderId = orderData.data.orderId;
    const paymentId = `pay_mock_${Date.now()}`;
    const signature = `sig_verified_${Date.now()}`;

    // 2. Verify Payment
    const verifyRes = await fetch(`${API_BASE}/payments/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId,
        paymentId,
        signature,
        amount: totalAmount
      })
    });
    const verifyData = await verifyRes.json();
    if (!verifyData.success || !verifyData.data.verified) {
      showToast('Payment signature verification failed', 'error');
      btn.disabled = false;
      btn.textContent = 'Authorize & Pay Now';
      return;
    }

    // 3. Confirm Reservation
    state.pendingBookingPayload.paymentId = paymentId;
    const bookRes = await fetch(`${API_BASE}/reservations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(state.pendingBookingPayload)
    });
    const bookData = await bookRes.json();

    if (bookRes.ok && bookData.success) {
      closeModal('paymentModal');
      closeModal('bookingModal');
      showToast(`Reservation Confirmed! Reference: ${bookData.data.pnr}`, 'success');
      displayTicketModal(bookData.data);
      loadInitialTrains();
      if (state.currentUser) loadUserBookings('ALL');
    } else {
      showToast(bookData.message || 'Reservation failed after payment', 'error');
    }
  } catch (err) {
    console.error('Payment checkout error:', err);
    showToast('Payment processing error', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Authorize & Pay Now';
  }
}

// ===================================================================
// E-Ticket Display & Modal
// ===================================================================
function displayTicketModal(ticket) {
  document.getElementById('ticket-pnr').textContent = ticket.pnr;

  const officialPnrTag = document.getElementById('ticket-official-pnr-tag');
  const disclaimerText = document.getElementById('ticket-disclaimer-text');
  const bookingTypeBrand = document.getElementById('ticket-booking-type-brand');

  if (ticket.officialPnr) {
    officialPnrTag.textContent = `Official Indian Railways PNR: ${ticket.officialPnr}`;
    disclaimerText.innerHTML = `<strong>OFFICIAL RAILWAY BOOKING:</strong> Ticket registered with Indian Railways PRS. Official PNR: ${escapeHtml(ticket.officialPnr)}`;
    bookingTypeBrand.textContent = `🚆 OFFICIAL IRCTC / PRS RESERVATION`;
  } else {
    officialPnrTag.textContent = `App Ref: ${ticket.pnr}`;
    disclaimerText.innerHTML = `<strong>APPLICATION RESERVATION:</strong> Application-generated booking reference. Not registered with official PRS.`;
    bookingTypeBrand.textContent = `🚆 APPLICATION RESERVATION`;
  }

  document.getElementById('ticket-train').textContent = `${ticket.trainName} (${ticket.trainNumber})`;
  document.getElementById('ticket-date').textContent = formatDateDisplay(ticket.journeyDate);
  document.getElementById('ticket-from').textContent = ticket.source;
  document.getElementById('ticket-to').textContent = ticket.destination;
  document.getElementById('ticket-dept').textContent = ticket.departureTime || '--';
  document.getElementById('ticket-arr').textContent = ticket.arrivalTime || '--';

  document.getElementById('ticket-seat').textContent = ticket.seatNumber;
  document.getElementById('ticket-class').textContent = ticket.travelClass;
  document.getElementById('ticket-fare').textContent = `₹${ticket.fare}`;

  const passengersContainer = document.getElementById('ticket-passengers-table');
  if (ticket.passengers && ticket.passengers.length > 0) {
    passengersContainer.innerHTML = `
      <table style="width: 100%; font-size: 0.85rem; border-collapse: collapse; margin-top: 4px;">
        <thead>
          <tr style="background: #f1f5f9; text-align: left;">
            <th style="padding: 4px 8px;">#</th>
            <th style="padding: 4px 8px;">Name</th>
            <th style="padding: 4px 8px;">Age/Gender</th>
            <th style="padding: 4px 8px;">Berth</th>
            <th style="padding: 4px 8px;">Status</th>
          </tr>
        </thead>
        <tbody>
          ${ticket.passengers.map((p, i) => `
            <tr style="border-bottom: 1px solid #e2e8f0;">
              <td style="padding: 4px 8px;">${i + 1}</td>
              <td style="padding: 4px 8px; font-weight: 600;">${escapeHtml(p.name)}</td>
              <td style="padding: 4px 8px;">${p.age}y / ${escapeHtml(p.gender)}</td>
              <td style="padding: 4px 8px;">${escapeHtml(p.allocatedBerth || p.berthPreference || '--')}</td>
              <td style="padding: 4px 8px; color: #166534; font-weight: 600;">CONFIRMED</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } else {
    passengersContainer.innerHTML = `<div><strong>${escapeHtml(ticket.passengerName)}</strong> (${ticket.age}y / ${escapeHtml(ticket.gender)})</div>`;
  }

  const statusEl = document.getElementById('ticket-status');
  if (ticket.status === 'CONFIRMED') {
    statusEl.innerHTML = '<span class="badge badge-confirmed">CONFIRMED</span>';
  } else {
    statusEl.innerHTML = '<span class="badge badge-cancelled">CANCELLED</span>';
  }

  document.getElementById('ticket-booked-at').textContent = ticket.bookingTime || new Date().toLocaleString();
  openModal('ticketModal');
}

// ===================================================================
// My Bookings
// ===================================================================
async function loadUserBookings(filter = 'ALL') {
  state.activeFilter = filter;
  const container = document.getElementById('bookings-container');
  container.innerHTML = '<div style="text-align:center; padding: 2rem;">Loading reservations...</div>';

  try {
    let url = `${API_BASE}/reservations`;
    if (state.currentUser) {
      url = `${API_BASE}/reservations/user/${state.currentUser.id}`;
    }

    const res = await fetch(url);
    const data = await res.json();

    if (data.success) {
      state.userBookings = data.data || [];
      renderBookings(state.userBookings, filter);
    } else {
      container.innerHTML = `<div class="empty-state"><p>${data.message || 'Failed to load bookings'}</p></div>`;
    }
  } catch (err) {
    console.error('Bookings load error:', err);
    container.innerHTML = `<div class="empty-state"><p>Failed to connect to server</p></div>`;
  }
}

function renderBookings(bookings, filter) {
  const container = document.getElementById('bookings-container');
  let filtered = bookings;
  if (filter !== 'ALL') {
    filtered = bookings.filter(b => b.status === filter);
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>No Reservations Found</h3>
        <p>${filter !== 'ALL' ? `No ${filter.toLowerCase()} reservations found.` : 'You have not reserved any tickets yet.'}</p>
        <button class="btn btn-primary btn-sm" onclick="showSection('home')">Search & Reserve Trains</button>
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="custom-table">
        <thead>
          <tr>
            <th>PNR / Reference</th>
            <th>Passenger(s)</th>
            <th>Train</th>
            <th>Route</th>
            <th>Journey Date</th>
            <th>Seat(s)</th>
            <th>Class</th>
            <th>Fare</th>
            <th>Status</th>
            <th style="text-align: right;">Actions</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map(b => `
            <tr>
              <td>
                <strong><code>${escapeHtml(b.pnr)}</code></strong>
                ${b.officialPnr ? `<br><small style="color: #047857; font-weight: 600;">Official: ${escapeHtml(b.officialPnr)}</small>` : ''}
              </td>
              <td>${escapeHtml(b.passengerName)} <br><small style="color:var(--text-muted);">${b.age}y, ${escapeHtml(b.gender)}</small></td>
              <td>${escapeHtml(b.trainName)} <br><small style="color:var(--text-muted);">#${escapeHtml(b.trainNumber)}</small></td>
              <td>${escapeHtml(b.source)} ➔ ${escapeHtml(b.destination)}</td>
              <td>${formatDateDisplay(b.journeyDate)}</td>
              <td><span style="font-weight:700; color:#065f46; background:#ecfdf5; padding:2px 8px; border-radius:4px;">${escapeHtml(b.seatNumber)}</span></td>
              <td>${escapeHtml(b.travelClass)}</td>
              <td>₹${b.fare}</td>
              <td>
                <span class="badge ${b.status === 'CONFIRMED' ? 'badge-confirmed' : 'badge-cancelled'}">
                  ${escapeHtml(b.status)}
                </span>
              </td>
              <td style="text-align: right; white-space: nowrap;">
                <button class="btn btn-outline btn-sm" onclick='viewTicketByPnr("${escapeHtml(b.pnr)}")'>Slip</button>
                ${b.status === 'CONFIRMED' ? `
                  <button class="btn btn-danger btn-sm" onclick='promptCancelTicket("${escapeHtml(b.pnr)}", "${escapeHtml(b.passengerName)}", "${escapeHtml(b.seatNumber)}")'>Cancel</button>
                ` : ''}
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

// ===================================================================
// PNR Lookup & Cancellation
// ===================================================================
async function handlePnrLookup(e) {
  e.preventDefault();
  const pnr = document.getElementById('pnr-input').value.trim().toUpperCase();
  const container = document.getElementById('pnr-result-container');
  container.innerHTML = '<div style="text-align:center; padding: 2rem;">Searching PNR Status...</div>';

  try {
    const res = await fetch(`${API_BASE}/pnr/${encodeURIComponent(pnr)}`);
    const data = await res.json();

    if (data.success && data.data) {
      const pnrData = data.data;
      const isOfficial = pnrData.pnrType === 'OFFICIAL_IRCTC_PRS';

      container.innerHTML = `
        <div class="train-card" style="margin-top: 1rem;">
          <div class="train-main-info">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span class="train-badge">${isOfficial ? 'OFFICIAL INDIAN RAILWAYS PNR' : 'APPLICATION BOOKING REFERENCE'}: ${escapeHtml(pnrData.pnr)}</span>
              <span class="badge badge-confirmed">${escapeHtml(pnrData.chartStatus || 'CHART NOT PREPARED')}</span>
            </div>
            <h3 class="train-name">${escapeHtml(pnrData.trainName)} (#${escapeHtml(pnrData.trainNumber)})</h3>
            <div class="train-route">
              ${escapeHtml(pnrData.source)} (${escapeHtml(pnrData.sourceCode || '')}) ➔ ${escapeHtml(pnrData.destination)} (${escapeHtml(pnrData.destinationCode || '')})
            </div>
            <div style="margin-top: 0.5rem;">
              <strong>Date of Journey:</strong> ${formatDateDisplay(pnrData.dateOfJourney)} | 
              <strong>Class:</strong> ${escapeHtml(pnrData.travelClass)} | 
              <strong>Quota:</strong> ${escapeHtml(pnrData.quota)}
            </div>
          </div>

          <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
            <div style="font-size: 0.85rem; color: var(--text-muted);">Data Source</div>
            <span style="font-size: 0.8rem; font-weight: 700; color: #1e3a8a; background: #eff6ff; padding: 3px 8px; border-radius: 4px; margin-top: 4px;">
              ${escapeHtml(pnrData.dataSource)}
            </span>
          </div>

          <div class="train-booking-action">
            <div style="display: flex; gap: 0.5rem; width: 100%; justify-content: flex-end;">
              <button class="btn btn-outline" onclick='viewTicketByPnr("${escapeHtml(pnrData.pnr)}")'>View Ticket Slip</button>
            </div>
          </div>
        </div>

        <div style="background: white; border: 1px solid var(--border-color); border-radius: 8px; padding: 1rem; margin-top: 1rem;">
          <h4 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.5rem;">Passenger Booking Status</h4>
          <table style="width: 100%; font-size: 0.85rem; border-collapse: collapse;">
            <thead>
              <tr style="background: #f8fafc; text-align: left;">
                <th style="padding: 6px 10px;">#</th>
                <th style="padding: 6px 10px;">Passenger Name</th>
                <th style="padding: 6px 10px;">Booking Status</th>
                <th style="padding: 6px 10px;">Current Status</th>
                <th style="padding: 6px 10px;">Coach / Berth</th>
              </tr>
            </thead>
            <tbody>
              ${pnrData.passengers.map((p, i) => `
                <tr style="border-bottom: 1px solid #f1f5f9;">
                  <td style="padding: 6px 10px;">${i + 1}</td>
                  <td style="padding: 6px 10px; font-weight: 600;">${escapeHtml(p.name)}</td>
                  <td style="padding: 6px 10px;">${escapeHtml(p.bookingStatus)}</td>
                  <td style="padding: 6px 10px; font-weight: 700; color: #166534;">${escapeHtml(p.currentStatus)}</td>
                  <td style="padding: 6px 10px; font-weight: 600;">${escapeHtml(p.berthNumber || '--')}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">❌</div>
          <h3>PNR Not Found</h3>
          <p>${escapeHtml(data.message || 'No reservation record found for this PNR or Booking Reference.')}</p>
        </div>
      `;
    }
  } catch (err) {
    console.error('PNR lookup error:', err);
    showToast('Failed to check PNR status', 'error');
  }
}

async function viewTicketByPnr(pnr) {
  try {
    const res = await fetch(`${API_BASE}/reservations/${encodeURIComponent(pnr)}`);
    const data = await res.json();
    if (data.success && data.data) {
      displayTicketModal(data.data);
    } else {
      showToast('Could not load reservation details', 'error');
    }
  } catch (err) {
    console.error('Ticket view error:', err);
    showToast('Failed to retrieve reservation slip', 'error');
  }
}

function promptCancelTicket(pnr, passengerName, seatNumber) {
  state.pnrPendingCancellation = pnr;
  document.getElementById('cancel-modal-pnr').textContent = pnr;
  document.getElementById('cancel-modal-passenger').textContent = passengerName;
  document.getElementById('cancel-modal-seat').textContent = seatNumber;
  openModal('cancelConfirmModal');
}

async function executeCancelReservation() {
  const pnr = state.pnrPendingCancellation;
  if (!pnr) return;

  const btn = document.getElementById('cancel-modal-confirm-btn');
  btn.disabled = true;
  btn.textContent = 'Cancelling...';

  try {
    let url = `${API_BASE}/reservations/${encodeURIComponent(pnr)}`;
    if (state.currentUser && state.currentUser.id) {
      url += `?userId=${state.currentUser.id}`;
    }
    const res = await fetch(url, {
      method: 'DELETE'
    });
    const data = await res.json();

    if (data.success) {
      closeModal('cancelConfirmModal');
      showToast(`Reservation cancelled! Refund: ₹${data.data.refundAmount}`, 'success');
      loadInitialTrains();
      loadUserBookings(state.activeFilter);
    } else {
      showToast(data.message || 'Failed to cancel reservation', 'error');
    }
  } catch (err) {
    console.error('Cancellation error:', err);
    showToast('Server error while cancelling reservation', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Yes, Cancel & Refund';
  }
}

// ===================================================================
// Authentication & Profile Management
// ===================================================================
function updateAuthUI() {
  const container = document.getElementById('auth-section');
  const navProfile = document.getElementById('nav-profile');
  if (!container) return;

  if (state.currentUser) {
    if (navProfile) navProfile.style.display = 'inline-block';
    container.innerHTML = `
      <div class="user-menu" style="display: flex; align-items: center; gap: 0.5rem;">
        <div class="user-avatar" onclick="showSection('profile')" style="cursor: pointer;">${escapeHtml(state.currentUser.name.charAt(0).toUpperCase())}</div>
        <span style="cursor: pointer; font-weight: 600;" onclick="showSection('profile')">${escapeHtml(state.currentUser.name)}</span>
        <button class="btn btn-outline btn-sm" onclick="logout()" style="margin-left: 0.5rem; padding: 2px 8px; font-size: 0.75rem;">Logout</button>
      </div>
    `;
  } else {
    if (navProfile) navProfile.style.display = 'none';
    container.innerHTML = `
      <button class="btn btn-outline btn-sm" onclick="openLoginModal()">Sign In</button>
      <button class="btn btn-primary btn-sm" onclick="openRegisterModal()">Register</button>
    `;
  }
}

function openLoginModal() { openModal('loginModal'); }
function openRegisterModal() { openModal('registerModal'); }

function fillDemoCredentials() {
  document.getElementById('login-email').value = 'tarun@example.com';
  document.getElementById('login-password').value = 'password123';
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;

  const btn = document.getElementById('login-submit-btn');
  btn.disabled = true;
  btn.textContent = 'Signing in...';

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();

    if (res.ok && data.success) {
      state.currentUser = data.data;
      localStorage.setItem('railway_user', JSON.stringify(data.data));
      updateAuthUI();
      closeModal('loginModal');
      showToast(`Welcome back, ${data.data.name}!`, 'success');
      loadUserBookings('ALL');
    } else {
      showToast(data.message || 'Invalid email or password', 'error');
    }
  } catch (err) {
    console.error('Login error:', err);
    showToast('Failed to connect to authentication service', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Sign In';
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();
  const name = document.getElementById('reg-name').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const phone = document.getElementById('reg-phone').value.trim();
  const password = document.getElementById('reg-password').value;
  const confirmPassword = document.getElementById('reg-confirm-password').value;

  if (password !== confirmPassword) {
    showToast('Passwords do not match', 'error');
    return;
  }

  const btn = document.getElementById('reg-submit-btn');
  btn.disabled = true;
  btn.textContent = 'Creating account...';

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, phone, password, confirmPassword })
    });
    const data = await res.json();

    if (res.ok && data.success) {
      state.currentUser = data.data;
      localStorage.setItem('railway_user', JSON.stringify(data.data));
      updateAuthUI();
      closeModal('registerModal');
      showToast(`Account registered! Welcome, ${data.data.name}`, 'success');
    } else {
      showToast(data.message || 'Registration failed', 'error');
    }
  } catch (err) {
    console.error('Register error:', err);
    showToast('Failed to register account', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Create Account';
  }
}

function loadUserProfile() {
  if (!state.currentUser) {
    showToast('Please sign in to view your profile', 'info');
    openLoginModal();
    return;
  }
  document.getElementById('profile-name').value = state.currentUser.name || '';
  document.getElementById('profile-email').value = state.currentUser.email || '';
  document.getElementById('profile-phone').value = state.currentUser.phone || '';
  document.getElementById('profile-role').value = state.currentUser.role || 'USER';
}

async function handleProfileUpdate(e) {
  e.preventDefault();
  if (!state.currentUser) return;

  const name = document.getElementById('profile-name').value.trim();
  const phone = document.getElementById('profile-phone').value.trim();

  try {
    const res = await fetch(`${API_BASE}/users/${state.currentUser.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, phone })
    });
    const data = await res.json();

    if (data.success) {
      state.currentUser.name = data.data.name;
      state.currentUser.phone = data.data.phone;
      localStorage.setItem('railway_user', JSON.stringify(state.currentUser));
      updateAuthUI();
      showToast('Profile updated successfully!', 'success');
    } else {
      showToast(data.message || 'Failed to update profile', 'error');
    }
  } catch (err) {
    console.error('Profile update error:', err);
    showToast('Error updating profile', 'error');
  }
}

function logout() {
  state.currentUser = null;
  localStorage.removeItem('railway_user');
  updateAuthUI();
  showToast('Logged out successfully', 'info');
  showSection('home');
}

// ===================================================================
// Modal & Toast Helpers
// ===================================================================
function openModal(modalId) {
  const el = document.getElementById(modalId);
  if (el) el.classList.add('active');
}

function closeModal(modalId) {
  const el = document.getElementById(modalId);
  if (el) el.classList.remove('active');
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function formatDateDisplay(dateStr) {
  if (!dateStr) return '';
  try {
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const date = new Date(parts[0], parts[1] - 1, parts[2]);
      return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    }
    return dateStr;
  } catch (e) {
    return dateStr;
  }
}

function escapeHtml(text) {
  if (text === null || text === undefined) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
