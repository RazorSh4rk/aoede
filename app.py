from flask import Flask, request

app = Flask(__name__)

@app.route("/")
def response():
    code = request.args.get("code")
    return "<h1>use this code: </h1><h3>"+code+"</h3>"

app.run(host="0.0.0.0", port=9005)