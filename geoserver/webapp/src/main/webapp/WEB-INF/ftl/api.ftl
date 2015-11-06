<html>
<head>
<title>Composer API Configuration</title>
<meta name="ROBOTS" content="NOINDEX, NOFOLLOW"/>
</head>
<body>
<h2>Composer API Configuration</h2>
<#if model["requests"]?size != 0>
<ul>
<#foreach request in model["requests"]>
<li>${request["method"]} <a href="${model["baseUrl"]}${request["pattern"]}">${request["pattern"]}</a></li>
</#foreach>
</ul>
<#else>
<p>There are no REST extensions installed.  If you expected some, please verify your installation (did you restart the server?).</p>
</#if>
</body>
</html>