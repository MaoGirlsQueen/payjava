<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>微信支付</title>
</head>
<body>
<script src="https://cdn.bootcss.com/jquery/1.5.1/jquery.min.js"></script>
<script type="text/javascript" src="https://cdn.bootcss.com/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>
<div id="my"></div>
<script>
    jQuery('#my').qrcode({
        text: "${codeUrl}"
    });
</script>

</body>
</html>
