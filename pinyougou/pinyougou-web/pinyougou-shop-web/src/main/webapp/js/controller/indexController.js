/** 定义后台主页的控制器 */
app.controller('indexController', function ($scope, baseService) {

    // 获取登录用户名
    $scope.showLoginName = function () {
        baseService.sendGet("/showLoginName").then(function(response){
            // 获取响应数据 {}
            $scope.loginName = response.data.loginName;
        });
    };
});