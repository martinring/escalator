ws = new WebSocket(routes.controllers.Scala.session().webSocketURL())

editor = null
fline = 1
line = 1
code = ""

CodeMirror.commands.execute = (cm) ->
  editor = cm
  cm.setOption('readonly',true)
  fline = line
  ffline = line
  ws.send('+'+cm.getDoc().getValue())
  
ws.onmessage = (e) -> #if lastEditor?    
  type   = e.data[0]
  result = e.data[1..]
  if type is '?'
    i = result.indexOf(':')
    pos = parseInt(result[0..i])
    cs = result[i+1..].split(';')
    CodeMirror.showHint(editor,(cm,opt) ->
      doc = cm.getDoc()
      from = doc.posFromIndex(pos)
      to = doc.getCursor()
      text = doc.getRange(from,to)
      return (
        list: cs.filter((s) -> s.substring(0,text.length) is text)
        from: from
        to: to)
      )            
  else
    doc = editor.getDoc()
    if code is '' and doc.getLine(line - fline) is ''
      fline += 1
      editor.removeLine(0)      
    else
      code += '\n' + doc.getLine(line - fline)
    line += 1
    editor.setOption('firstLineNumber',line)
    console.log(line,e.data,code)  
    switch type
      when '+'
        neditor = CodeMirror document.getElementById("flow"),
          value: code[1..]
          lineNumbers: true
          firstLineNumber: fline
          mode: 'text/x-scala'
          readOnly: 'nocursor'
        $('#flow').append("<pre class='ok'>#{result}</pre>")
        while fline < line 
          editor.removeLine(0)
          fline += 1      
        code = ""
      when '-'
        neditor = CodeMirror document.getElementById("flow"),
          value: code[1..]
          lineNumbers: true   
          firstLineNumber: fline 
          mode: 'text/x-scala'
          readOnly: 'nocursor'          
        $('#flow').append("<pre class='error'>#{result}</pre>")
        while fline < line 
          editor.removeLine(0)
          fline += 1
        code = ""      

CodeMirror.commands.autocomplete = (cm) ->
  editor = cm
  doc = cm.getDoc()
  cur = doc.getCursor()
  ws.send("?" + doc.indexFromPos(cur) + ":" + cm.getDoc().getValue())

$ ->  
  editor = CodeMirror.fromTextArea document.getElementById("code"),
    lineNumbers: true
    matchBrackets: true
    mode: 'text/x-scala'
    autofocus: true
    extraKeys:
      'Ctrl-Enter' : 'execute'
      'Ctrl-Space' : 'autocomplete'  