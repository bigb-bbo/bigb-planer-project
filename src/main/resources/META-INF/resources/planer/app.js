const { useState } = React;

function App() {
  const defaultPlayers = Array.from({ length: 10 }, (_, i) => `Player ${i + 1}`);
  const [players, setPlayers] = useState(defaultPlayers);
  const [rounds, setRounds] = useState(5);
  const [output, setOutput] = useState('');
  const [status, setStatus] = useState('');

  function updatePlayer(index, value) {
    const copy = [...players];
    copy[index] = value;
    setPlayers(copy);
  }

  async function callHealth() {
    setStatus('calling health...');
    try {
      const res = await fetch('/planer/health');
      const text = await res.text();
      setOutput(text);
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
      const res = await fetch('/planer/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      const json = await res.json();
      setOutput(JSON.stringify(json, null, 2));
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function getPairings() {
    setStatus('loading pairings...');
    try {
      const res = await fetch('/planer/pairings');
      const json = await res.json();
      setOutput(JSON.stringify(json, null, 2));
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function getPlayerUsage() {
    setStatus('loading player usage...');
    try {
      const res = await fetch('/planer/player-usage');
      const json = await res.json();
      setOutput(JSON.stringify(json, null, 2));
      setStatus(`HTTP ${res.status}`);
    } catch (e) {
      setOutput(e.toString());
      setStatus('error');
    }
  }

  async function getStatistics() {
    setStatus('loading statistics...');
    try {
      const res = await fetch('/planer/statistics');
      const json = await res.json();
      setOutput(JSON.stringify(json, null, 2));
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
          </div>
        </div>
      </div>

      <div className="output">
        <h2>Status: {status}</h2>
        <pre>{output}</pre>
      </div>

      <footer>
        <small>Calls backend endpoints under <code>/planer/*</code></small>
      </footer>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(React.createElement(App));

