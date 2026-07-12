import os
import re
from flask import Flask, request, jsonify

app = Flask(__name__)

# Deontic keywords mapping
DEONTIC_PROHIBITION = ["shall not", "must not", "prohibited", "forbidden", "cannot", "restricts", "restrict"]
DEONTIC_OBLIGATION = ["shall", "must", "obliged", "obligated", "required", "mandated"]
DEONTIC_EXEMPTION = ["exempt", "exemption", "except", "unless", "exemption clause"]

@app.route('/extract', methods=['POST'])
def extract_rules():
    """
    POST endpoint to parse CNL regulatory text and classify entities and deontic operators.
    Expects JSON: {"text": "raw text statement"}
    """
    data = request.get_json()
    if not data or 'text' not in data:
        return jsonify({"error": "Missing 'text' parameter"}), 400

    raw_text = data['text'].strip()
    if not raw_text:
        return jsonify({"error": "Empty text"}), 400

    # 1. Classify Deontic Operator
    normalized = raw_text.lower()
    operator = "PERMISSION"
    
    if any(kw in normalized for kw in DEONTIC_PROHIBITION):
        operator = "PROHIBITION"
    elif any(kw in normalized for kw in DEONTIC_EXEMPTION):
        operator = "EXEMPTION"
    elif any(kw in normalized for kw in DEONTIC_OBLIGATION):
        operator = "OBLIGATION"

    # 2. Extract Constraint (text after if/unless/subject to/except when)
    constraint = ""
    constraint_match = re.search(r'(?i)(?:if|unless|except when|subject to)\s+(.+)$', raw_text)
    if constraint_match:
        constraint = constraint_match.group(1).strip()
        if constraint.endswith('.'):
            constraint = constraint[:-1]

    # Isolate the core sentence without the constraint suffix
    base_sentence = raw_text
    if constraint_match:
        base_sentence = raw_text[:constraint_match.start()].strip()

    # 3. Extract Subject, Action, Target
    subject = "controller"
    action = "transfer"
    target = "personal_data"

    # Match common CNL patterns: "Subject modalVerb Action Target"
    # e.g. "a controller may transfer personal data"
    sentence_match = re.match(
        r'(?i)^(.+?)\s+(?:shall not|must not|shall|must|may|can|should|is obliged to|is permitted to)\s+(.+?)\s+(.+)$',
        base_sentence
    )
    
    if sentence_match:
        subject = sentence_match.group(1).strip()
        action = sentence_match.group(2).strip()
        target = sentence_match.group(3).strip()
        if target.endswith('.'):
            target = target[:-1]
    else:
        # Fallbacks: keyword scan
        if "transfer" in normalized:
            action = "transfer"
        elif "process" in normalized:
            action = "process"
        elif "store" in normalized:
            action = "store"

    response = {
        "operator": operator,
        "subject": subject,
        "action": action,
        "target": target,
        "constraint": constraint,
        "raw_text": raw_text
    }
    
    return jsonify(response)

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5001))
    # Bind to all interfaces for container networking
    app.run(host='0.0.0.0', port=port, debug=False)
