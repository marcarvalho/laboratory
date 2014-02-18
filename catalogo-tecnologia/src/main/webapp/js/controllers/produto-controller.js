'use strict';

/* Controllers */
var controllers = angular.module('catalogo.controllers');

controllers.controller('ProdutoList',
		function Produto($scope, $http, $location) {

			function carregarProdutos() {
				$http.get('api/produto').success(function(data) {
					$scope.produtos = data;
				});
			}

			$scope.novoProduto = function() {
				$location.path('/produto/edit');
			};

			$scope.editarProduto = function(produto) {
				$location.path('/produto/edit/' + produto.id);
			};

			$scope.excluirProduto = function(id) {
				$http({
					url : 'api/produto/' + id,
					method : "DELETE"

				}).success(function(data) {
					carregarProdutos();

				}).error(function(data, status) {
				});
			};

			carregarProdutos();
		});

controllers.controller('ProdutoEdit', function Produto($scope, $http,
		$location, $routeParams, $upload, $rootScope, AlertService) {

	var id = $routeParams.id;
	
	// Necessário para compartilhar entre os controladores: Anexo, Colaboradores...
	$rootScope.demandaId = id;

	if (id) {
		$http.get('api/produto/' + id).success(function(data) {
			$scope.produto = data;
			$scope.plataformasSuportadas = data.plataformasSuportadas;
		});
	} else {
		$scope.produto = {};
		$scope.plataformasSuportadas = [];
	}

	$scope.salvarProduto = function() {
		console.log("ProdutoController " + $scope.produto);
		$scope.produto.plataformasSuportadas = $scope.plataformasSuportadas;
		$("[id$='-message']").text("");
		$http({
			url : 'api/produto',
			method : $scope.produto.id ? "PUT" : "POST",
			data : $scope.produto,
			headers : {
				'Content-Type' : 'application/json;charset=utf8'
			}

		}).success(function(data) {
			AlertService.addWithTimeout('success','Produto salvo com sucesso');
			$location.path('produto');
		}).error(
				function(data, status) {
					if (status = 412) {
						$.each(data, function(i, violation) {
							$("#" + violation.property + "-message").text(
									violation.message);
						});
					}
				});

	};
	
	$scope.adicionaPlataforma = function() {
		var index = buscaElemento($scope.plataforma);
		
		if (index !== -1) {
			alert('Plataforma já foi adicionada!');
        }else{
			$scope.plataformasSuportadas.push($scope.plataforma);
		}
	};
	
	$scope.removePlataforma = function(plataforma) {
		var index = buscaElemento(plataforma);
			
		if (index !== -1) {
            $scope.plataformasSuportadas.splice(index,1);
        }
	};
	
	function buscaElemento(plataforma){
		var index = -1;
		for ( var i = 0 ; i < $scope.plataformasSuportadas.length ; i++ ) {
			if ($scope.plataformasSuportadas[i] === plataforma) {
                index = i;
                break;
            }
		}
		return index;
	}

});