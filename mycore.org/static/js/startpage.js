 $(document).ready(function() {
         $(".sphereLink").hover(

              function () {
                  var sphere = $(this).children('img.sphere');
                  sphere.attr("src", "./images/start/abbildung_orange_110x110.jpg");
              },
              function () {
                  var sphere = $(this).children('img.sphere');
                  switch(sphere.attr("rel")){
                      case "sphere_manysided":
                          sphere.attr("src", "./images/start/abbildung_vielseitig.jpg");
                          break;
                      case "sphere_adaptable":
                          sphere.attr("src", "./images/start/abbildung_anpassbar.jpg");
                          break;
                      case "sphere_lasting":
                          sphere.attr("src", "./images/start/abbildung_nachhaltig.jpg");
                          break;
                  }
              }
          );
      });