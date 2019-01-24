/** 定义基础控制器 */
app.controller('baseController', function ($scope, baseService) {

    // 获取登录用户名
    $scope.loadUsername = function () {
        // 重定向URL编码 unicode编码
        // location.href: 获取用户浏览器地址栏中的URL
        $scope.redirectURL = window.encodeURIComponent(location.href);
        // 发异步请求
        baseService.sendGet("/user/showName").then(function(response){
            // 获取响应数据
            $scope.loginName = response.data.loginName;
        });
    };
});