const { useState, useEffect } = React;

function App() {
  const defaultPlayers = Array.from({ length: 10 }, (_, i) => `Player ${i + 1}`);
  const [players, setPlayers] = useState(defaultPlayers);
  const [rounds, setRounds] = useState(30);
  const [output, setOutput] = useState('');
  const [status, setStatus] = useState('');

  useEffect(() => {
    // Load initial player names from a local JSON file served at /planer/players.json
    // Expected formats: ["A","B",...] or { "playerNames": ["A", ...] }
    async function loadPlayers() {
      try {
        const res = await fetch('/planer/players.json');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const data = await res.json();
        if (Array.isArray(data) && data.every(p => typeof p === 'string')) {
          setPlayers(data);
          setStatus('Loaded players from /planer/players.json');
          return;
        }
        if (data && Array.isArray(data.playerNames) && data.playerNames.every(p => typeof p === 'string')) {
          setPlayers(data.playerNames);
          setStatus('Loaded players from /planer/players.json');
          return;
        }
        setStatus('players.json has invalid format; using defaults');
      } catch (e) {
        setStatus('Could not load players.json; using defaults');
      }
    }
    loadPlayers();
  }, []);

  function updatePlayer(index, value) {
    const copy = [...players];
    copy[index] = value;
    setPlayers(copy);
  }

  async function callHealth() {
    setStatus('calling health...');
    try {
      const res = await fetch('/api/planer/health');
      const text = await res.text();
      let json;
      try {
        json = JSON.parse(text);
        setOutput(JSON.stringify(json, null, 2));
      } catch (parseErr) {
        setOutput(text);
      }
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function generatePlan() {
    setStatus('generating...');
    const body = { playerNames: players, numberOfRounds: Number(rounds) };
    try {
      const res = await fetch('/api/planer/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      const text = await res.text();
      let json;
      try {
        json = JSON.parse(text);
        setOutput(JSON.stringify(json, null, 2));
      } catch (parseErr) {
        setOutput(`Response was not JSON: ${text}`);
      }
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function getPairings() {
    setStatus('loading pairings...');
    try {
      const res = await fetch('/api/planer/pairings');
      const text = await res.text();
      let json;
      try {
        json = JSON.parse(text);
        setOutput(JSON.stringify(json, null, 2));
      } catch (parseErr) {
        setOutput(`Response was not JSON: ${text}`);
      }
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function getPlayerUsage() {
    setStatus('loading player usage...');
    try {
      const res = await fetch('/api/planer/player-usage');
      const text = await res.text();
      let json;
      try {
        json = JSON.parse(text);
        setOutput(JSON.stringify(json, null, 2));
      } catch (parseErr) {
        setOutput(`Response was not JSON: ${text}`);
      }
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function getStatistics() {
    setStatus('loading statistics...');
    try {
      const res = await fetch('/api/planer/statistics');
      const text = await res.text();
      let json;
      try {
        json = JSON.parse(text);
        setOutput(JSON.stringify(json, null, 2));
      } catch (parseErr) {
        setOutput(`Response was not JSON: ${text}`);
      }
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  // New: download generated plan as XLS-compatible CSV
  async function downloadPlan() {
    setStatus('downloading plan...');
    try {
      const res = await fetch('/api/planer/download');
      if (!res.ok) {
        const text = await res.text();
        setOutput(text);
        setStatus(`HTTP ${res.status}`);
        return;
      }
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      // try to parse filename from content-disposition header
      const cd = res.headers.get('Content-Disposition');
      let filename = 'plan.csv';
      if (cd) {
        const match = cd.match(/filename=\s*"?([^";]+)"?/i);
        if (match) filename = match[1];
      }
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  return (
    <div className="container">
      <h1>Planer UI</h1>
      <div className="controls">
        <div className="players">
          <h2>Players</h2>
          {players.map((p, i) => (
            <input key={i} value={p} onChange={(e) => updatePlayer(i, e.target.value)} />
          ))}
        </div>
        <div className="config">
          <label>Rounds: <input type="number" value={rounds} min={1} onChange={(e) => setRounds(e.target.value)} /></label>
          <div className="buttons">
            <button onClick={generatePlan}>Generate Plan</button>
            <button onClick={getPairings}>Pairings</button>
            <button onClick={getPlayerUsage}>Player Usage</button>
            <button onClick={getStatistics}>Statistics</button>
            <button onClick={callHealth}>Health</button>
            <button onClick={downloadPlan}>Download Plan (XLS)</button>
          </div>
        </div>
      </div>

      <div className="output">
        <h2>Status: {status}</h2>
        <pre>{output}</pre>
      </div>

      <footer>
        <small>Calls backend endpoints under <code>/api/planer/*</code> — player names are loaded from <code>/planer/players.json</code></small>
      </footer>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(React.createElement(App));
