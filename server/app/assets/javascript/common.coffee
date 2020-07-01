
@toURLParameter = (obj) ->
  xs = ("#{key}=#{value}" for key, value of obj when value?)
  xs.join('&')

@fromURLParameter = (str) ->
  obj = {}
  for kv in str.split('&')
    ary = kv.split('=')
    key = decodeURIComponent(ary.shift())
    obj[key] = decodeURIComponent(ary.join('='))
  obj
