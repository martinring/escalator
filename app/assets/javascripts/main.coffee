
socket = new WebSocket(routes.controllers.Scala.session().webSocketURL())

cmStage = null
cmInput = null
cmStageLines = null

$ ->
  cmInit()
  $('#link-run').click((e) ->
    e.preventDefault()
    CodeMirror.commands.execute(cmInput)
  )
  $('.link-more').click((e) ->
    e.preventDefault()
    more = $(this).siblings('.more')
    isClosed = more.css('display') is 'none';
    $('#sidebar').find('.more').slideUp()
    more.slideDown() if isClosed
  )
  
  
  
  
  

cmInit = (theme = 'default') ->
  cmStageLines = []
  cmStageOptions =
    value: ''
    mode: 'text/x-scala'
    theme: theme
    readOnly: true
    autofocus: false
    matchBrackets: true
    lineWrapping: true
    lineNumbers: true
    firstLineNumber: 0
    lineNumberFormatter: (line) ->
      idx = cmStageLines.indexOf(line)
      if idx < 0 then '' else idx + 1
  cmInputOptions =
    value: ''
    mode: 'text/x-scala'
    theme: theme
    indentUnit: 2
    smartIndent: false
    indentWithTabs: false
    lineWrapping: true
    lineNumbers: true
    firstLineNumber: 1
    readOnly: false
    undoDepth: 32
    tabindex: 1
    autofocus: true
    dragDrop: false
    matchBrackets: true
    extraKeys:
      'Ctrl-Enter'     : 'execute'
      'Ctrl-Space'     : 'autocomplete'
      'Ctrl-Backspace' : 'clear'
  cmStage = CodeMirror(document.getElementById("content"), cmStageOptions)
  cmInput = CodeMirror(document.getElementById("content"), cmInputOptions)
  editors = $('#content').children('.CodeMirror')
  $(editors[0]).attr('id', 'cmStage')
  $(editors[1]).attr('id', 'cmInput')




# status:
# 0 = code with linenumber

cmPrint = (text, status) ->
  cmDoc = cmStage.getDoc()
  txtSize = text.split('\n').length
  if cmDoc.getValue() is ''
    cmLines = [0...txtSize]
    cmFrom =
      line: 0
      ch: 0
  else
    text = '\n' + text
    cmSize = cmDoc.lineCount()
    cmLines = [cmSize...(cmSize + txtSize)]
    cmFrom =
      line: cmSize - 1
      ch: cmDoc.getLine(cmSize - 1).length
  cmStageLines = cmStageLines.concat(cmLines) if status is 0
  cmDoc.replaceRange(text, cmFrom)
  cmInput.setOption('firstLineNumber', cmStageLines.length + 1)








  
socket.onmessage = (e) ->
  type   = e.data[0]
  result = e.data[1..]
  if type is '?'
    i = result.indexOf(':')
    pos = parseInt(result[0..i])
    cs = result[i+1..].split(';')
    CodeMirror.showHint(cmInput, (cm, opt) ->
      console.log cm
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
    cmPrint(result, type)





CodeMirror.commands.execute = (cm) ->
  msg = cm.getDoc().getValue()
  socket.send('+' + msg)
  cmPrint(msg, 0)
  cm.setValue('')

CodeMirror.commands.autocomplete = (cm) ->
  doc = cm.getDoc()
  cur = doc.getCursor()
  socket.send("?" + doc.indexFromPos(cur) + ":" + cm.getDoc().getValue())

CodeMirror.commands.clear = (cm) ->
  cm.getDoc().setValue('')
