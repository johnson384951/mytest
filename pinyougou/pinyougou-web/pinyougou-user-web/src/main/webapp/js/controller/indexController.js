/** 定义控制器层 */
app.controller('indexController', function($scope, baseService){

    // 定义显示用户名方法
    $scope.showName = function () {
        baseService.sendGet("/user/showName").then(function (response) {
            // 获取登录用户名
            $scope.loginName = response.data.loginName;
        });
    };

});