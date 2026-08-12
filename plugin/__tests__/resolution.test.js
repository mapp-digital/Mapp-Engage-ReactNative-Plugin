describe('packaged config plugin', () => {
  it('loads compiled JavaScript from app.plugin.js', () => {
    expect(require('../../app.plugin')).toEqual(expect.any(Function));
  });
});

