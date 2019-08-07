const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const HtmlWebpackPluginConfig = new HtmlWebpackPlugin({
  template: "./index.html",
  filename: "index.html",
  inject: "body"
});

module.exports = {
  mode: "development",
  context: path.join(__dirname, "src"),
  entry: ["./index.js"],
  output: {
    path: path.join(__dirname, "dist"),
    filename: "bundle.js"
  },
  module: {
    rules: [
      {
        test: /\.(png|jpg|gif)$/i,
        loader: ["url-loader"]
      },
      {
        test: /\.css$/,
        loader: ["style-loader", "css-loader"]
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/,
        use: {
          loader: "file-loader",
          options: {
            outputPath: "fonts"
          }
        }
      },
      {
        test: /\.(js|jsx)$/,
        exclude: /(node_modules|__test__)/,
        use: ["babel-loader"]
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: ["babel-loader", "eslint-loader"]
      },
      {
        test: /\.svg$/,
        use: "raw-loader"
      }
    ]
  },
  plugins: [HtmlWebpackPluginConfig]
};
